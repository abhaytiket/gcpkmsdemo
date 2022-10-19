# Base image
FROM debian:latest

ARG JAVA_VERSION_FOR_EXTENSION="19-open"
ARG JAVA_VERSION="11.0.12-open"
ARG INSTALL_MAVEN="true"
ARG MAVEN_VERSION="3.6.3"
ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get -qq install curl git zip

# Install sdkman
RUN curl -s "https://get.sdkman.io" | bash

# Switch from `sh -c` to `bash -c` as the shell behind a `RUN` command.
SHELL ["/bin/bash", "-c"]

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install java "${JAVA_VERSION}" && sdk install java "${JAVA_VERSION_FOR_EXTENSION}" && sdk default java "${JAVA_VERSION_FOR_EXTENSION}"

# [Option] Install Maven / Gradle
RUN if [ "${INSTALL_MAVEN}" = "true" ]; then source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install maven "${MAVEN_VERSION}"; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install gradle "${GRADLE_VERSION}"; fi

COPY .m2/settings.xml /root/.m2/settings.xml