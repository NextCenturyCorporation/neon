/*
 * Copyright 2015 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.userimport

/**
 * Simple container that denotes that a given value failed to be converted to a given type. Used by the convertValueToType method of ImportUtilities.
 */
class ConversionFailureResult {

	Object value
	String type

	@Override
	String toString() {
		return "Failed to convert object ${value.toString} of type ${value.getClass()} to type $type."
	}
}