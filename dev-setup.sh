#!/bin/bash
# Development Environment Setup for Critical Maps Android
# Sets up proper Java version and environment for building the project

export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "🔧 Android Development Environment Ready!"
echo "Java Version: $(java -version 2>&1 | head -n 1)"
echo "JAVA_HOME: $JAVA_HOME"
echo ""
echo "Available commands:"
echo "  ./gradlew assembleDebug     - Build debug APK"
echo "  ./gradlew lint              - Run lint checks"
echo "  ./gradlew testDebugUnitTest - Run unit tests"
echo "  ./gradlew clean             - Clean build"
echo ""
echo "To use: source ./dev-setup.sh"