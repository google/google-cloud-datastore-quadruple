#  Copyright 2025 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

JAVA_TEST=../java/quadruple-converter/src/test/java/com/google/cloud/datastore/core/quadruple/QuadrupleBuilderTest.java
PYTHON_TEST=../python/quadruple_builder_test.py

all: $(JAVA_TEST) $(PYTHON_TEST)

$(JAVA_TEST): java/start.m4 edgecases.m4 testcases.m4 java/end.m4 Makefile
	m4 java/start.m4 edgecases.m4 testcases.m4 java/end.m4 | sed -e 's/#/\/\//' > "$@"

$(PYTHON_TEST): python/start.m4 edgecases.m4 testcases.m4 python/end.m4 Makefile
	m4 python/start.m4 edgecases.m4 testcases.m4 python/end.m4 > "$@"
