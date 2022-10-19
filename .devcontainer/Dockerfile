# Base image
FROM debian:latest

ARG JAVA_VERSION="19-open"
ARG INSTALL_MAVEN="true"
ARG MAVEN_VERSION="3.8.6"
ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get -qq install curl git zip

# Install sdkman
RUN curl -s "https://get.sdkman.io" | bash

# Switch to `bash -c` as the shell behind a `RUN` command.
SHELL ["/bin/bash", "-c"]

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install java "${JAVA_VERSION}" && sdk default java "${JAVA_VERSION}"

# [Option] Install Maven / Gradle
RUN if [ "${INSTALL_MAVEN}" = "true" ]; then source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install maven "${MAVEN_VERSION}"; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install gradle "${GRADLE_VERSION}"; fi