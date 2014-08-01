The NeonAngularDemo package contains all of the R functions that this example calls using OpenCPU.
OpenCPU makes it easy to call a single R function, but it makes it difficult to chain functions
together. This is deliberate, because you are expected to create your own package that does
everything you need.

To build this R package, follow the usual methods:

    R CMD INSTALL NeonAngularDemo
    R CMD build NeonAngularDemo

Normally, a package is checked for correctness with the following command, but this package still
has documentation errors that will cause it to fail the tests:

    R CMD check NeonAngularDemo

To install this on an OpenCPU server, copy the resulting tar.gz file to that server and run

    sudo R
    > install.packages('NeonAngularDemo_x.y.tar.gz', repos=NULL)

where *x.y* is replaced with the version of the package.
