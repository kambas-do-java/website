#!/usr/bin/env bash

set -e

echo "Gerando site estático..."

echo "Executando gerador..."
java --enable-preview --source 24 main.java

echo "Site gerado em site/"
