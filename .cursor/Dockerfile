# Cloud Agent base image for the Charge-me Not Android app.
# Provides the stable toolchain (JDK 17 + Android SDK 36) required to build the
# project. Repository dependencies (Gradle distribution + Maven artifacts) are
# refreshed by the environment `install` command after checkout.
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless \
        git \
        curl \
        unzip \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${JAVA_HOME}/bin:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

# Pinned Android command-line tools release (sdkmanager).
ARG ANDROID_CMDLINE_TOOLS_VERSION=11076708

RUN mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && mv "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" "${ANDROID_SDK_ROOT}/cmdline-tools/latest" \
    && rm /tmp/cmdline-tools.zip \
    && yes | sdkmanager --licenses > /dev/null \
    && sdkmanager --install "platform-tools" "platforms;android-36" "build-tools;36.0.0" > /dev/null \
    && chown -R ubuntu:ubuntu "${ANDROID_SDK_ROOT}"

USER ubuntu
