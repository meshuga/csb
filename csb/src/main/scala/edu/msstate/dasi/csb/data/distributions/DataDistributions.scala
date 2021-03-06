package edu.msstate.dasi.csb.data.distributions

import edu.msstate.dasi.csb.model.{ConnStates, EdgeData, Protocols, VertexData}
import org.apache.spark.graphx.{EdgeDirection, Graph, VertexId}

/**
 * Contains all the probability distributions of the domain. All conditional
 * distributions are conditioned by originator bytes values.
 *
 * @param graph the graph used to compute the distributions
 */
class DataDistributions(graph: Graph[VertexData, EdgeData]) extends Serializable {

  /**
    * The in-degree distribution.
    */
  val inDegree: Distribution[Int] = Distribution(graph.collectNeighborIds(EdgeDirection.In)
    .flatMap{record => for (v <- record._2) yield (record._1, v.asInstanceOf[VertexId])}
    .map((_, 1))
    .reduceByKey(_ + _)
    .values)

  /**
    * The out-degree distribution.
    */
  val outDegree: Distribution[Int] = Distribution(
    graph.collectNeighborIds(EdgeDirection.Out)
      .flatMap{record => for (v <- record._2) yield (record._1, v.asInstanceOf[VertexId])}
      .map((_, 1))
      .reduceByKey(_ + _)
      .values)

  /**
   * The timestamp distribution.
   */
  val ts: Distribution[Long] = Distribution(graph.edges.map(e => e.attr.ts))

  /**
   * The originator's port number distribution.
   */
  val origPort: Distribution[Int] = Distribution(graph.edges.map(e => e.attr.origPort))

  /**
   * The responder’s port number distribution.
   */
  val respPort: Distribution[Int] = Distribution(graph.edges.map(e => e.attr.respPort))

  /**
   * The originator's bytes distribution.
   */
  val origBytes: Distribution[Long] = Distribution(graph.edges.map(e => e.attr.origBytes))

  /**
   * The protocol conditional distribution.
   */
  val proto: ConditionalDistribution[Protocols.Value, Long] = {
    val data = graph.edges.map(e => (e.attr.proto, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The duration conditional distribution.
   */
  val duration: ConditionalDistribution[Double, Long] = {
    val data = graph.edges.map(e => (e.attr.duration, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The responder’s bytes conditional distribution.
   */
  val respBytes: ConditionalDistribution[Long, Long] = {
    val data = graph.edges.map(e => (e.attr.respBytes, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The connection state conditional distribution.
   */
  val connState: ConditionalDistribution[ConnStates.Value, Long] = {
    val data = graph.edges.map(e => (e.attr.connState, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The originator's packets conditional distribution.
   */
  val origPkts: ConditionalDistribution[Long, Long] = {
    val data = graph.edges.map(e => (e.attr.origPkts, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The originator's IP bytes conditional distribution.
   */
  val origIpBytes: ConditionalDistribution[Long, Long] = {
    val data = graph.edges.map(e => (e.attr.origIpBytes, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The responder’s packets conditional distribution.
   */
  val respPkts: ConditionalDistribution[Long, Long] = {
    val data = graph.edges.map(e => (e.attr.respPkts, e.attr.origBytes))
    new ConditionalDistribution(data)
  }

  /**
   * The responder’s IP bytes conditional distribution.
   */
  val respIpBytes: ConditionalDistribution[Long, Long] = {
    val data = graph.edges.map(e => (e.attr.respIpBytes, e.attr.origBytes))
    new ConditionalDistribution(data)
  }
}

/**
 * Factory for [[DataDistributions]] instances.
 */
object DataDistributions {
  /**
   * Builds a [[DataDistributions]] instance from a [[Graph]].
   *
   * @note the resulting distributions are expected to be small, as they will be loaded into the driver's memory.
   * @param graph      the input graph
   * @param bucketSize the normalization value to apply to each numeric value, non-negative values are ignored
   *
   * @return the resulting [[DataDistributions]] object
   */
  def apply(graph: Graph[VertexData, EdgeData], bucketSize: Int = 0): DataDistributions = {
    if (bucketSize > 0) {
      new DataDistributions(graph.mapEdges(e => e.attr.copy(
        ts = e.attr.ts - e.attr.ts % bucketSize,
        duration = e.attr.duration - e.attr.duration % bucketSize,
        origBytes = e.attr.origBytes - e.attr.origBytes % bucketSize,
        respBytes = e.attr.respBytes - e.attr.respBytes % bucketSize,
        origPkts = e.attr.origPkts - e.attr.origPkts % bucketSize,
        origIpBytes = e.attr.origIpBytes - e.attr.origIpBytes % bucketSize,
        respPkts = e.attr.respPkts - e.attr.respPkts % bucketSize,
        respIpBytes = e.attr.respIpBytes - e.attr.respIpBytes % bucketSize)))
    } else {
      new DataDistributions(graph)
    }
  }
}
