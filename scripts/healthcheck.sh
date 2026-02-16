#!/bin/bash
curl -f http://localhost:8080/api/health || exit 1
