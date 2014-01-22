/**
 * Creates a transform that can be applied to a query.
 * @param name The fully qualified name of the transform to be used.
 * @class neon.query.Transform
 * @constructor
 */
neon.query.Transform = function (name) {
    this.transformName = name;
    this.params = undefined;
};

/**
 * Allows one to parameterize a transform.
 * @param {Object} params Parameters to set on the transform.
 * @return {neon.query.Transform} A instance of the transform for method chaining
 * @method parameterizeTransforms
 */
neon.query.Transform.prototype.parameterizeTransform = function(params){
    this.params = params;
    return this;
};
