/*
 * Copyright 2013 Next Century Corporation
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

/**
 * Creates a transform that can be applied to a query.
 * @param name The fully qualified name of the transform to be used.
 * @class neon.query.Transform
 * @constructor
 */
neon.query.Transform = function(name) {
    this.transformName = name;
};

/**
 * Adds parameters to the transform
 * @param {Object} params Parameters to set on the transform.
 * @return {neon.query.Transform} This transform object
 * @method params
 */
neon.query.Transform.prototype.params = function(params) {
    this.params = params;
    return this;
};
