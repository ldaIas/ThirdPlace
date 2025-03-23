port module Utils.Ports exposing (socket, generateDID, didGenerated, authenticate, authenticationResult)

import Json.Encode
import Websockets


port webSocketCommand : Websockets.CommandPort msg


port webSocketEvent : Websockets.EventPort msg


socket : Websockets.Methods msg
socket =
    Websockets.withPorts
        { command = webSocketCommand
        , event = webSocketEvent
        }

port generateDID : () -> Cmd msg
port didGenerated : (Json.Encode.Value -> msg) -> Sub msg

port authenticate : ({did: String, privKey: String} -> Cmd msg)
port authenticationResult : (Bool -> msg) -> Sub msg