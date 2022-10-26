package com.tiket.gcpkmsdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.KmsAeadKeyManager;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Optional;

import javax.annotation.PostConstruct;

@RestController
public class CryptoController {
	private static final Logger logger = LoggerFactory.getLogger(CryptoController.class);
	
	@Value("${tink.gcpkms.credentials.file}")
	private String gcpCredentialFilename;

	@Value("${tink.gcpkms.kek.uri}")
	private String kekUri;

	@Value("${tink.gcpkms.keyfile}")
	private String keyFile;

	private Aead aead;
	private static final byte[] EMPTY_ASSOCIATED_DATA = new byte[0];

	@PostConstruct
	public void init() {
		try {
			// Register all AEAD key types with the Tink runtime.
			AeadConfig.register();
		} catch (GeneralSecurityException ex) {
			logger.error("Cannot register all AEAD key types with Tink runtime");
			logger.error(ex.toString());
			System.exit(1);
		}

		// Read the GCP credentials and set up client
		try {
			URL resource = CryptoController.class.getClassLoader().getResource(gcpCredentialFilename);
			String gcpCredentialFilePath = resource.getPath();
			GcpKmsClient.register(Optional.of(kekUri), Optional.of(gcpCredentialFilePath));
		} catch (GeneralSecurityException ex) {
			logger.error("Error initializing GCP client");
			logger.error(ex.toString());
			System.exit(1);
		}

		// From the key-encryption key (KEK) URI, create a remote AEAD primitive for encrypting Tink keysets.
		Aead kekAead = null;
		try {
			KeysetHandle handle = KeysetHandle.generateNew(KmsAeadKeyManager.createKeyTemplate(kekUri));
			kekAead = handle.getPrimitive(Aead.class);
		} catch (GeneralSecurityException ex) {
			logger.error("Error creating primitive");
			logger.error(ex.toString());
			System.exit(1);
		}
	
		// Use the primitive to encrypt text

		// Read the encrypted keyset
		KeysetHandle handle = null;
		try {
			File file = ResourceUtils.getFile("classpath:" + keyFile);
			handle = KeysetHandle.read(JsonKeysetReader.withFile(file), kekAead);
			aead = handle.getPrimitive(Aead.class);
		} catch (GeneralSecurityException | IOException ex) {
			logger.error("Error reading key and generating primitive");
			logger.error(ex.toString());
			System.exit(1);
		}
	}
	
	@GetMapping("/")
	public String index() {
		return "GCP KMS Demo Application";
	}

	@PostMapping("/encrypt")
	public String encrypt(@RequestBody String decstr) {
		String encstr = "Unable to encrypt";

		// Encrypt text
		try {
			byte[] input = decstr.getBytes();
			byte[] ciphertext = aead.encrypt(input, EMPTY_ASSOCIATED_DATA);
			encstr = Base64.getEncoder().encodeToString(ciphertext);
		} catch (GeneralSecurityException ex) {
			logger.error("Error on encryption");
			logger.error(ex.toString());
		}
		
		return encstr;
	}

	@PostMapping("/decrypt")
	public String decrypt(@RequestBody String encstr) {
		String decstr = "Unable to decrypt";
		
		// Decrypt text
		try {
			byte[] input = Base64.getDecoder().decode(encstr);
			byte[] plaintext = aead.decrypt(input, EMPTY_ASSOCIATED_DATA);
			decstr = new String(plaintext, StandardCharsets.US_ASCII);
		} catch (GeneralSecurityException ex) {
			logger.error("Error on decryption");
			logger.error(ex.toString());
		}
		
		return decstr;
	}
}
