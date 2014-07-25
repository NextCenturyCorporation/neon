\name{nsensorMMPP}
\alias{nsensorMMPP}
\title{
nstl2
}
\description{
Passes the arguments to the sensorMMPP function, and then returns just the probability of an event for each hour of each day.
}
\usage{
sensorMMPP(N, priors, ITER, EQUIV)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{N}{
See documentation of sensorMMPP
}
  \item{priors}{
See documentation of sensorMMPP
}
  \item{ITER}{
See documentation of sensorMMPP
}
  \item{EQUIV}{
See documentation of sensorMMPP
}
}
\details{
This is a fairly simple wrapper around sensorMMPP. It is needed because OpenCPU makes it difficult to chain functions.
}
\value{
The average across all iterations of sensorMMPP$Z, as described in the sensorMMPP documentation.
}
\references{
}
\author{
Next Century Corporation
}
\note{

}

\seealso{
}
\examples{
}
