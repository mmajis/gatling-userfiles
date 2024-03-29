
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._
import assertions._
//import com.excilys.ebi.gatling.core.scenario.Scenario
import com.excilys.ebi.gatling.core.structure.ScenarioBuilder

class ElasticitySimulation extends Simulation {

	val httpConf = httpConfig
			.baseURL("http://test.majakorpi.net")
			.acceptHeader("application/json")
			.userAgentHeader("curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8r zlib/1.2.5")


	val headers_1 = Map(
			"Content-Type" -> """application/json"""
	)

	val scn = scenarioInstance("First wave", 60*60*1000)
	val scn2 = scenarioInstance("Second wave", 40*60*1000)
	val scn3 = scenarioInstance("Third wave", 20*60*1000)

	setUp(scn.users(5).ramp(5).protocolConfig(httpConf))
	setUp(scn2.users(10).ramp(10).delay(10*60).protocolConfig(httpConf))
	setUp(scn3.users(100).ramp(60).delay(20*60).protocolConfig(httpConf))

	def scenarioInstance(name:String, duration:Long) : ScenarioBuilder = {
		return scenario(name)
		.exec((s:Session) => s.setAttribute("startTime", System.currentTimeMillis))
		.exec((s:Session) => s.setAttribute("duration", duration))
		.asLongAs(isContinue) {
			exec(http("request_1")
					.post("/testapp/meetings/reserveLoanNegotiation")
					.headers(headers_1)
						.fileBody("ElasticitySimulation_request_1.txt"))
			.pause(500 milliseconds, 800 milliseconds)
		}
	}

   def isContinue(session:Session) : Boolean = {
	  //println("sesContinue: " + ((System.currentTimeMillis - session.getTypedAttribute[Long]("startTime")) < (1000)))
      return (System.currentTimeMillis - session.getTypedAttribute[Long]("startTime")) < session.getTypedAttribute[Long]("duration")
   }
}