#!/usr/bin/env sh
set -eu

MAVEN_VERSION="${MAVEN_VERSION:-3.9.11}"
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
MVN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MVN_BIN="${MVN_HOME}/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  mkdir -p "$MVN_HOME"
  TMP_DIR="$(mktemp -d)"
  ARCHIVE="${TMP_DIR}/apache-maven-${MAVEN_VERSION}.tar.gz"
  URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$ARCHIVE"
  else
    echo "curl or wget is required to bootstrap Maven" >&2
    exit 1
  fi
  tar -xzf "$ARCHIVE" -C "$TMP_DIR"
  rm -rf "$MVN_HOME"
  mv "$TMP_DIR/apache-maven-${MAVEN_VERSION}" "$MVN_HOME"
  rm -rf "$TMP_DIR"
fi

exec "$MVN_BIN" -f "$BASE_DIR/pom.xml" "$@"
