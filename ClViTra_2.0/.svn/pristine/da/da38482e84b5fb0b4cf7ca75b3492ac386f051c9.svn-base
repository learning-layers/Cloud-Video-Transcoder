/*
 * TODO:
 * - allow for closing the websocket connection again...
 */

function LASClient(parameters) {	
	"use strict";
	var that = this;
	var session = {
			status: "-",
			username: "-",
			sessionId: "-",
			sessionTtl: 0
		};
	var touchInterval = null;
	var subscriptions = {};
	
	this.options = {
			host: "127.0.0.1:8080",
			// should not be used - only for cases where OpenID is not applicable
			basicAuth: null,
			keepAlive: true,
			websocket: null,
			useLocalStorage: true,
			//useWebSocket: false,
			useWebSocket: (window.WebSocket === undefined)? false: true,
			enforceWebsocketUsage: false,	
			websocketCallbacks : {
					counter: 0
			}
		};
	
	// make sure that a logger is present
	if(typeof logger === "undefined") {
		console.log("Logger undefined - instanciating own one!");
	
		if(typeof log4javascript === "undefined") {
			console.log("Warning: log4javascript is undefined! Trying to load library dynamically...");
			
			loadScript("../js/log4javascript.js", function() {
				window.logger = log4javascript.getLogger();
				logger.addAppender(new log4javascript.BrowserConsoleAppender());
				logger.setLevel(log4javascript.Level.ALL);
			});
		}
		else {
			window.logger = log4javascript.getLogger();
			logger.addAppender(new log4javascript.BrowserConsoleAppender());
			logger.setLevel(log4javascript.Level.ALL);
		}
	}
	
	this.login = function(parameters, callback) {
		try {
			if(typeof parameters === "undefined" || parameters === null) {
				parameters = {};
				logger.warn("Called login without providing parameters. Falling back to default values...");
			}
			if(typeof callback === "undefined" || callback === null) {
				callback = function() {};
				logger.warn("Called login without providing a callback function.");
			}
			if(typeof parameters.openidIdentifier === "undefined" || parameters.openidIdentifier === null) {
				parameters.openidIdentifier = "http://" + that.options.host + "/openid";
			}
			
			// check options for desired type of login/authorization
			if((typeof parameters.iFrameDivId !== "undefined" && parameters.iFrameDivId !== null) || (typeof parameters.username !== "undefined" && parameters.username !== null && typeof parameters.password !== "undefined")) {
				var iFrameDiv = null;
				
				if(typeof parameters.iFrameDivId !== "undefined" && parameters.iFrameDivId !== null) {
					// use iFrame for OpenId-based login, manual login
					iFrameDiv = document.getElementById(parameters.iFrameDivId);
				}
				else {
					// use iFrame for OpenId-based login, automatic login for debugging/testing
					// only works with LAS Login!
					iFrameDiv = document.body;
				}
				
				var openIdMessageHandler = function(event) {
					try {
					    if(event.origin !== "http://" + that.options.host && event.origin !== "https://" + that.options.host) {
					        logger.warn("WARNING: the message origin is not as expected. Is: " + event.origin + " Expected: " + that.options.host);
					    }
					    
					    if(event.data === "LAS Login window ready" && (typeof parameters.username !== "undefined" && parameters.username !== null && typeof parameters.password !== "undefined")) {
					        if(typeof(window.frames[0].postMessage) !== "undefined") {
								// use web messaging
								var message = {
										username: parameters.username,
										password: parameters.password
								};
								window.frames[0].postMessage(message,'*'); // should use lasHost instead of *...
							}
					    }
					    else {
						    session.sessionId = event.data;
						    saveSession(session);
						   
						    // remove the event listener
						    window.removeEventListener("message", openIdMessageHandler, false );
						    
						    // remove the iFrame again...
						    try {
						        iFrameDiv.removeChild(iFrameElement); 					    
						    }
						    catch(error) {
						        logger.warn("Exception when removing the login iFrame element: " + error);
						    }
						    
						    // establish WebSocket connection...
						    connectViaWebSocket(function() {
						    	 callback(true);
						    });
					    }
					}
					catch(error) {
						logger.error("Handling the OpenID message failed: " + error);
					}
				};
				
				window.addEventListener("message", openIdMessageHandler, false );
				
				var iFrameElement = document.createElement("iframe");
				iFrameElement.src = "http://" + that.options.host+ "/openid_login?openid_identifier='" + parameters.openidIdentifier + "'";
				iFrameDiv.appendChild(iFrameElement);
			}
			else if(typeof parameters.basicAuthCredentials !== "undefined" && parameters.basicAuthCredentials !== null) {
				// use basic auth credentials (not recommended)
				console.log("basic...");
				throw "TODO";
			}
			else {
				// use popup for OpenId-based login				
				window.addEventListener("message", function(event) {
				    if(event.origin !== "http://" + that.options.host && event.origin !== "https://" + that.options.host) {
				        logger.warn("WARNING: the message origin is not as expected. Is: " + event.origin + " Expected: " + that.options.host);
				    }
				    session.sessionId = event.data;
				    saveSession(session);
				    
				    // establish WebSocket connection...
				    connectViaWebSocket(function() {
				    	 callback(true);
				    });
				}, false );
				
				window.open("http://" + that.options.host+"/openid_login?openid_identifier='"+parameters.openidIdentifier+"'", 'OpenID Login', 'width=880,height=550,location=1,status=1,resizable=yes');
			}
		}
		catch(error) {
			logger.error("LAS login failed: " + error);
			callback(null);
		}
	};
	
	this.logout = function(callback) {
		try {
			if(typeof callback === "undefined" || callback === null) {
				logger.warn("Logout was called without providing a callback");
				callback = function() {};
			}
			
			deleteSession();
			
			if(that.options.websocket !== null) {
				try {
					that.options.websocket.close();
					that.options.websocket = null;
				}
				catch(error) {
					logger.warn("Closing the websocket connection failed: " + error);
				}
			}
			
			var request = new XMLHttpRequest();  
			request.open("GET", "http://" + that.options.host + "/openid_logout?sessionId=" + session.sessionId, true);  
			request.onload = function(e) {
				callback(true);
			};
			request.send(); 
			
		}
		catch(error) {
			logger.error("LAS logout failed: " + error);
			callback(null);
		}
	};
	
	this.getUsername = function(callback) {
		try {
			if(session.username === "-" && session.sessionId !== "-") {
				getStatus(function() {
					// has to get it from the server first...
					callback(session.username);
				});
			}
			else {
				callback(session.username);
			}
		}
		catch(error) {
			logger.error("Getting LAS session username failed: " + error);
			throw error;
		}
	};
	
	this.getStatus = function(callback) {
		try {
			var request = new XMLHttpRequest();  
			request.open("GET", "http://" + that.options.host + "/openid_status?sessionId=" + session.sessionId, true);  
			request.onload = function(e) {
				if(request.responseText !== null) {
					saveSession(JSON.parse(request.responseText));

					callback(session);
					
					//clean up...
					if(session.status !== "logged in") {
						deleteSession();
					}
					return;
				}
				
				callback(null);
			};
			request.send(); 
		}
		catch(error) {
			logger.error("Getting LAS session status failed: " + error);
			throw error;
		}
	};
	
	this.sendRequest = function(method, path, parameters, contentType, onLoad, onError) {
		try {		
			if(typeof method === "undefined" || method === null) {
				throw "Must provide the method as parameter!";
			}
			if(typeof path === "undefined" || path === null) {
				throw "Must provide the path as parameter!";
			}
			if(path.substring(0, 1) !== "/") {
				path = "/" + path;
			}
			if(path.substring(0, 6) !== "/rest/") {
				path = "/rest" + path;
			}
			if(typeof parameters === "undefined" || parameters === null) {
				parameters = {};
			}
			if(typeof contentType === "undefined" || contentType === null || contentType === "") {
				contentType = "application/JSON";
			}
			if(typeof onLoad === "undefined") {
				logger.warn("Sending rest request without defining a onLoad callback");
				onLoad = function() {};
			}
			if(typeof onError === "undefined") {
				logger.warn("Sending rest request without defining a onError callback");
				onError = function() {};
			}
			
			var requestData = {
					protocol: "RESTRequest",
					messageId: null,
					method: method,
					path: path,
					contentType: contentType,
					authorization: (session.sessionId === "-") ? that.options.basicAuth : session.sessionId,
					parameters: parameters
			};
			
			if(that.options.useWebSocket && that.options.enforceWebsocketUsage && that.options.websocket === null) {
				logger.info("Websocket connectionnot ready - retrying...");
				setTimeout(function() {
					that.sendRequest(method, path, parameters, contentType, onLoad, onError);
				}, 250);
				return;
			}
			else if(that.options.useWebSocket && that.options.websocket !== null) {				
				requestData.messageId = that.options.websocketCallbacks.counter++;
				that.options.websocketCallbacks["message"+requestData.messageId] = {
					onLoad: onLoad,
					onError: onError
				};
				
				that.options.websocket.send(JSON.stringify(requestData));
			}
			else {
				var request = new XMLHttpRequest();  
				
				request.onload = function(e) {
					if(request.status < 400) {
						onLoad(request.responseText);
					}
					else {
						onError({status: request.status, message: request.responseText});
					}
				};
				request.onError = function(e) {
					onError({status: request.status, message: request.responseText});
				};
				
				request.open(requestData.method, "http://" + that.options.host + requestData.path);
				
				if(typeof requestData.authorization !== "undefined" && requestData.authorization !== null) {
					request.setRequestHeader('Authorization', requestData.authorization);
				}
				
				if(typeof requestData.contentType !== "undefined" && requestData.contentType !== null) {
					request.setRequestHeader('Content-Type', requestData.contentType);
				}

				if(isEmpty(requestData.parameters)) {
					request.send(JSON.stringify({}));
				}
				else {
					// send parameter values which are not encoded by the path
					var encodedParameters = "";
					var name = null;
					
					//iterate through the parameters...
					for(name in requestData.parameters) {
						var value = requestData.parameters[name];
						
						if(encodedParameters.length>1) {
							encodedParameters += "&";
						}
						
						encodedParameters += encodeURIComponent(name).replace("%20", "+") + "=" + encodeURIComponent(value).replace("%20", "+");
					}
					
					request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
					request.send(encodeURIComponent(encodedParameters));
				}
			}
		}
		catch(error) {
			logger.error("Sending request failed: " + error);
			throw error;
		}
	};
	
	/**
	 * Helper function which simplifies download files from any URL. Note that this method does not set/use
	 * any authentication, therefore this only works for publicly accessible files.
	 * 
	 * @param {String} url URL of the file to download
	 * @param {String} contentType as string. Defaults: if (null && !isBinary), then "application/JSON" will be assumed, if(null && binary), then "image/png"
	 * @param {Boolean} isBinary
	 * @param {function(result)} onLoad callback, if isBinary the response is returned as Uint8Array. Otherwise as text
	 * @param {function({status, message})} onError callback
	 */
	this.downloadFile = function(url, contentType, isBinary, onLoad, onError) {
		try {
			var request = new XMLHttpRequest(); 
			request.open("GET", url, true);
			
			if(isBinary) {
				request.responseType = "arraybuffer";
				
				if(contentType === null || contentType === "") {
					contentType = "image/png";
				}
			}			
			if(contentType === null || contentType === "") {
				contentType = "application/JSON";
			}
			
			request.onload = function(e) {
				if(request.status < 400) {
					if(isBinary) {
						var byteArray = null;
						var arrayBuffer = request.response;
						if(arrayBuffer) {
							byteArray = new Uint8Array(arrayBuffer);
						}
						onLoad(byteArray);
					}
					else {
						onLoad(request.responseText);
					}
				}
				else {
					onError({status: request.status, message: request.responseText});
				}
			};
			request.onError = function(e) {
				onError({status: request.status, message: request.responseText});
			};
			
			request.setRequestHeader('Content-Type', contentType);
			request.send(null);
		}
		catch(error) {
			logger.error("downloadFile failed: " + error);
			throw error;
		}
	};
	
	this.isSessionAvailable = function() {
		try {
			if(session.sessionId !== "-") {
				return true;
			}
			
			// see if there is a stored session
			var retrievedSession = null;
			
			// look in session storage first
			retrievedSession = sessionStorage.getItem("lasSession");
			
			if(retrievedSession == null && that.options.useLocalStorage){
				// maybe in the local storage?
				retrievedSession = localStorage.getItem("lasSession");
			}
			
			if(retrievedSession !== null) {
				retrievedSession = JSON.parse(retrievedSession);
			}			
			if(retrievedSession !== null
					&& typeof(retrievedSession.sessionId) !== "undefined"
					&& retrievedSession.sessionId !== "-") {
				
				saveSession(retrievedSession);
				connectViaWebSocket();
				return true;
			}
			
			return false;
		}
		catch(error) {
			logger.error("isSessionAvailable failed: " + error);
			throw error;
		}
	};
	
	this.touchSession = function(onLoad, onError) {
		if(typeof(onLoad) === "undefined" || onLoad === null) {
			onLoad = function() {};
		}
		if(typeof(onError) === "undefined" || onError === null) {
			onError = function() {};
		}
		
		if(that.isSessionAvailable()) {
			that.sendRequest("PUT", "/rest/touchsession", null, null, function() {
				onLoad();
			}, function(error) {
				logger.warn("Touching the LAS Session failed!");
				deleteSession();
				if(that.options.websocket !== null) {
					that.options.websocket.close();
					that.options.websocket = null;
				}
				
				onError(error);
			});
		}
	};
	
	this.subscribeNode = function(nodeName, onEvent, onLoad, onError) {
		if(typeof(onLoad) === "undefined" || onLoad === null) {
			onLoad = function() {};
		}
		if(typeof(onError) === "undefined" || onError === null) {
			onError = function() {};
		}
		
		if(that.options.useWebSocket) {
			if(that.options.websocket === null) {
				setTimeout(function() {
					that.subscribeNode(nodeName, onEvent, onLoad, onError);
				}, 500);
				return;
			}
			
			var requestData = {
					protocol: "NodeSubscription",
					messageId: null,
					name: nodeName
			};
			
			requestData.messageId = that.options.websocketCallbacks.counter++;
			that.options.websocketCallbacks["message"+requestData.messageId] = {
				onLoad: function() {
					// add event handler
					subscriptions[nodeName] = onEvent;
					onLoad();
				},
				onError: function(error) {
					logger.error("Failed to subscribe to the event: " + nodeName);
					onError(error);
				}
			};
			
			that.options.websocket.send(JSON.stringify(requestData));
		}
		else {
			logger.warn("Can not subscribe to event " + nodeName + " because WebSockets are not available (yet)!");
			//throw "Can not subscribe to event " + nodeName + " because WebSockets are not available!";
			setTimeout(function() {
				that.subscribeNode(nodeName, onEvent, onLoad, onError);
			}, 1000);
		}
	};
	
	this.unsubscribeNode = function(nodeName, onLoad, onError) {
		if(typeof(onLoad) === "undefined" || onLoad === null) {
			onLoad = function() {};
		}
		if(typeof(onError) === "undefined" || onError === null) {
			onError = function() {};
		}
		
		if(that.options.useWebSocket) {
			if(that.options.websocket === null) {
				onError({code: -1, message: "No WebSocket connection available!"});
				return;
			}
			
			var requestData = {
					protocol: "NodeUnsubscription",
					messageId: null,
					name: nodeName
			};
			
			requestData.messageId = that.options.websocketCallbacks.counter++;
			that.options.websocketCallbacks["message"+requestData.messageId] = {
				onLoad: function() {
					// remove event handler
					delete subscriptions[nodeName];
					onLoad();
				},
				onError: function(error) {
					logger.error("Failed to unsubscribe node: " + nodeName);
					onError(error);
				}
			};
			
			that.options.websocket.send(JSON.stringify(requestData));
		}
		else {
			logger.error("Can not unsubscribe node " + nodeName + " because WebSockets are not available!");
			throw "Can not unsibscribe node " + nodeName + " because WebSockets are not available!";
		}
	};
	
	function connectViaWebSocket(onOpen) {
		try {
			if(typeof(onOpen) === "undefined" || onOpen === null) {
				onOpen = function() {};
			}
			
			if(!that.options.useWebSocket) {
				onOpen();
				return;
			}
			
			if(!that.isSessionAvailable()) {
				throw "Can't establish WebSocket connection: there is no LAS Session available!";
			}
			
			if(that.options.websocket !== null) {
				onOpen();
				return;
			}
			
			// lock websocket temporary...
			that.options.useWebSocket = false;
			
			var ws = new WebSocket("ws://" + that.options.host + "/WebSocket/" + session.sessionId, "LASClient");

			ws.onopen = function(event) {
				logger.info("WebSocket Connection opened!");
				// unlock websocket again...
				that.options.useWebSocket = true;
				onOpen();
			};
			
			ws.onmessage = function(event) {			
				if(event.data === null) {
					logger.error("Received empty websocket message! " + event);
					return;
				}
				
				var data = JSON.parse(event.data);
				
				// Push message?
				if(typeof(data.type) !== "undefined" && data.type === "LASPush") {
					if(typeof(data.node) !== "undefined" && data.node !== null) {
						var onEvent = subscriptions[data.node];
						
						if(typeof(onEvent) !== "function" ||  onEvent === null) {
							logger.warn("Event from node " + data.node + " is not connected to any handler!");
						}
						else {
							onEvent(JSON.parse(data.payload));
						}
					}
					return;
				}
			
				// REST message
				if(typeof(data.status) === "undefined" || data.status === null) {
					logger.error("Received invalid websocket message (status not included)! " + event);
					return;
				}
				
				if(data.status < 200 || data.status >= 300) {
					// error occurred
					logger.error("Websocket: " + data.status + "-" + data.payload);
					if(typeof(data.messageId) !== "undefined" && data.messageId !== null) {
						if(that.options.websocketCallbacks["message"+data.messageId] !== "undefined" &&
								that.options.websocketCallbacks["message"+data.messageId].onError !== "undefined") {
							that.options.websocketCallbacks["message"+data.messageId].onError(data.status, data.payload);
							delete that.options.websocketCallbacks["message"+data.messageId];
						}
					}
				}
				else {				
					if(typeof(data.messageId) !== "undefined" && data.messageId !== null) {
						if(that.options.websocketCallbacks["message"+data.messageId] !== "undefined" &&
								that.options.websocketCallbacks["message"+data.messageId].onLoad !== "undefined") {
							that.options.websocketCallbacks["message"+data.messageId].onLoad(data.payload);
							delete that.options.websocketCallbacks["message"+data.messageId];
						}
					}
					else {
						logger.info("Received a WebSocket message which does not contain a message id.");
					}
				}
			};
			
			ws.onclose = function(event) {
				logger.info("WebSocket connection closed.");
				that.options.websocket = null;
			};
			
			// this makes sure the connection does not stay open if the tab/windows/browser is closed
			window.onbeforeunload = function() {
				if(that.options.websocket) {
					that.options.websocket.onclose = function () {
				    	that.options.websocket = null;
				    };
				    that.options.websocket.close();
				}
			};
			
			that.options.websocket = ws;
		}
		catch(error) {
			logger.error("Connecting to LAS via WebSocket failed: " + error);
			throw error;
		}
	}
	
	/**
	 * Saves the LAS session in a local variable as well as in HTML5 session storage
	 * 
	 * @param {Object} sessionObject JSON object with the session information
	 */
	function saveSession(sessionObject) {
		try {
			if(typeof sessionObject === "undefined" || sessionObject === null) {
				throw "Must provide the session information as parameter!";
			}
			if(typeof sessionObject.sessionId === "undefined" || sessionObject.sessionId === null) {
				throw "Must provide the session sessionId as parameter (sessionObject.sessionId)! Provided instead: " +  sessionObject.sessionId;
			}
			if(typeof sessionObject.status === "undefined" || sessionObject.status === null) {
				sessionObject.status = "-";
			}
			if(typeof sessionObject.username === "undefined" || sessionObject.username === null) {
				sessionObject.username = "-";
			}
			if(typeof sessionObject.sessionTtl === "undefined" || sessionObject.sessionTtl === null) {
				sessionObject.sessionTtl = 0;
			}
			
			session = sessionObject;
			sessionStorage.setItem("lasSession", JSON.stringify(session));
			if(that.options.useLocalStorage) {
				localStorage.setItem("lasSession", JSON.stringify(session));
			}
		}
		catch(error) {
			logger.error(error);
			throw error;
		}
	}
	
	/**
	 * Clears/deletes  LAS session information, incl. in the HTML5 session storage
	 */
	function deleteSession() {
		try {
			if(touchInterval != null) {
				clearInterval(touchInterval);
				touchInterval = null;
			}
			
			// clear the  session information...
			session.status = "-";
			session.username = "-";
			session.sessionId = "-";
			session.sessionTtl = 0;
			
			sessionStorage.setItem("lasSession", JSON.stringify(session));
			
			if(that.options.useLocalStorage) {
				localStorage.setItem("lasSession", JSON.stringify(session));
			}
		}
		catch(error) {
			logger.error("Clearing the stored LAS session failed: " + error);
			throw error;
		}
	}
	
	
	/**
	 * Helper method with combines/merges two objects
	 * 
	 * @param {Object} baseObject Base object
	 * @param {Object} addedObject Added object. If a property is already existent in baseObject, it is overwritten by addedObject's value.
	 * @returns {Object} Object with the merged key/values of both objects.
	 */
	function mergeObjects(baseObject, addedObject) {
		try {
			var mergedObject = {};
			var key = null;
			
			if(typeof baseObject === "undefined" || baseObject === null) {
				baseObject = {};
			}
			if(typeof addedObject === "undefined" || addedObject === null) {
				addedObject = {};
			}
			
			for(key in baseObject) {
				mergedObject[key] = baseObject[key];
			}
			for(key in addedObject) {
				mergedObject[key] = addedObject[key];
			}
			
			return mergedObject;
		}
		catch(error) {
			logger.error(error);
			throw error;
		}
	}
	
	/**
	 * Helper method which determines if a JSON object is empty
	 * 
	 * @param {Object} object that is to be tested for emptiness
	 * @returns {Boolean} true if empty, false otherwise
	 */
	function isEmpty(object) {
		var key = null;
		
		if(typeof object === "undefined" || object === null) {
			return true;
		}
		
	    if (object.length && object.length > 0) {
	        return false;
	    }

	    for (key in object) {
	        if (hasOwnProperty.call(object, key)) {
	            return false;
	        }
	    }

	    return true;
	}
	
	/**
	 * Helper method for loading missing libraries dynamically
	 * 
	 * @param {String} scriptURL URL of the script which is to be loaded
	 * @param {Function} onLoad is called after the script has been loaded
	 */
	function loadScript(scriptURL, onLoad) {
		if(typeof scriptURL === "undefined" || scriptURL === null) {
			throw "Must provide a valid script URL!";
		}
		if(typeof onLoad === "undefined" || onLoad === null) {
			onLoad = function() {};
		}
		
		var head = document.getElementsByTagName('head')[0];
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = scriptURL;
		script.onload = onLoad;
		head.appendChild(script);
	}
	
	function init() {
		that.options.host = "127.0.0.1:8080";
		
		that.options = mergeObjects(that.options, parameters);
		
		// make sure the LAS session is kept alive
		if(that.options.keepAlive) {
			touchInterval = setInterval(that.touchSession, 10000);
		}
	}
	
	init();
}
