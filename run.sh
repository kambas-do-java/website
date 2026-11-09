#!/usr/bin/env bash

set -e

echo "Gerando site estático..."

echo "Executando gerador..."
java main.java

echo "Site gerado em site/"
