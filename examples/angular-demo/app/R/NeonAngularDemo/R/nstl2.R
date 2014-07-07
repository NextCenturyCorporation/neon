nstl2 <-
function(x,n.p,t.degree,t.window,s.window,s.degree,outer) {
require(stl2)
fit <- stl2(x,n.p=n.p, t.degree=t.degree, t.window=t.window, s.window=s.window, s.degree=s.degree, outer=outer)
fit$data
}
