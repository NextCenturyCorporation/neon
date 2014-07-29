nsensorMMPP <- function (N, ITER) {
  require("mmppr")
  priors <- list()
  priors$aL <- 1
  priors$bL <- 1 #lambda0, baseline rate
  priors$aD <- rep(0,1,7)+5 #day effect dirichlet params
  priors$aH <- matrix(0,nrow=24,ncol=7)+1 #time of day effect dirichlet param
  priors$z01 <- .01*10000
  priors$z00 <- .99*10000   #z(t) event process
  
  priors$z01 <- .01*10000; priors$z00 = .99*10000;     # z(t) event process
  priors$z10 <- .25*10000; priors$z11 = .75*10000;     
  priors$aE <- 5; priors$bE <- 1/3;       # gamma(t), or NBin, for event # process
  
  priors$MODE <- 0;
  
  EQUIV <- c(3,3)
  samples <- sensorMMPP(N, priors, ITER, EQUIV)
}