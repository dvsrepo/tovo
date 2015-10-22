package es.upm.oeg.distance

/**
 * Method of measuring the similarity between two probability distributions. It is also known as Information Radius (IRad)
 * or Total Divergence to the Average.
 * It is based on the Kullback–Leibler divergence, with some differences, including that it is symmetric and it is always
 * a finite value.
 */
object JensenShannonDivergence {

  def apply (p: Array[Double], q: Array[Double]): Double ={
    var sumP : Double = 0.0
    var sumQ : Double = 0.0

    for (i <- Range(0, p.length)) {
      sumP += p(i) * Math.log( (2*p(i))/(p(i)+q(i)))
      sumQ += q(i) * Math.log( (2*q(i)/(p(i)+q(i))))
    }

    sumP + sumQ
  }

}
