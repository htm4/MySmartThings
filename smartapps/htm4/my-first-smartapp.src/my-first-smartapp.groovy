/**
 *  My First SmartApp
 *
 *  Copyright 2018 HTM4
 *
 *  GNU General Public License v3.0
 *  Permissions of this strong copyleft license are conditioned on making available complete source code of licensed 
 *  works and modifications, which include larger works using a licensed work, under the same license. Copyright and 
 *  license notices must be preserved. Contributors provide an express grant of patent rights.
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My First SmartApp",
    namespace: "htm4",
    author: "NA NA",
    description: "This describes the intent and functionality of your SmartApp. This description appears in the SmartApp Marketplace section of SmartThings mobile application, and hence a clear and concise description is recommended.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn on when motion detected:") {
        input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn off when there's been no movement for") {
        input "minutes", "number", required: true, title: "Minutes?"
    }
    section("Turn on this light") {
        input "theswitch", "capability.switch", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    theswitch.on()
}

def motionStoppedHandler(evt) {
    log.debug "motionStoppedHandler called: $evt"
    runIn(60 * minutes, checkMotion)
}

def checkMotion() {
    log.debug "In checkMotion scheduled method"

    // get the current state object for the motion sensor
    def motionState = themotion.currentState("motion")

    if (motionState.value == "inactive") {
            // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutes

            if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            theswitch.off()
            } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
            // Motion active; just log it and do nothing
            log.debug "Motion is active, do nothing and wait for inactive"
    }
}