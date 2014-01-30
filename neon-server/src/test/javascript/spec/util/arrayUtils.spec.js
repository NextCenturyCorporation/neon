
describe('array utils', function () {

    it('converts function arguments to an array', function () {
        var args;

        function setArgs() {
            args = arguments;
        }
        setArgs('a', 'b', 'c');



        var argsArray = neon.util.arrayUtils.argumentsToArray(args);
        expect(argsArray).toBeEqualArray(['a','b','c']);
    });

});