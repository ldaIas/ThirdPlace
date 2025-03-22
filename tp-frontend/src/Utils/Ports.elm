port module Utils.Ports exposing (socket, generateDID, didGenerated)

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