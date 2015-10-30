#!/bin/bash

curl --verbose localhost:8000/operations/health | python -m json.tool

