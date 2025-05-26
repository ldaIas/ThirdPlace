port module JSPorts.WebRTC.WebRTCPorts exposing (..)

-- Node Initialization Ports
port createConnection : () -> Cmd msg
port nodeInitialized : (NodeInfo -> msg) -> Sub msg
port nodeInitializationError : (String -> msg) -> Sub msg

-- Peer Connection Ports
port peerConnected : (PeerInfo -> msg) -> Sub msg
port peerDisconnected : (PeerInfo -> msg) -> Sub msg

-- WebRTC Signaling Ports (Sending)
port sendOffer : OfferData -> Cmd msg
port sendAnswer : AnswerData -> Cmd msg
port sendCandidate : CandidateData -> Cmd msg

-- WebRTC Signaling Ports (Receiving)
port receiveOffer : (OfferData -> msg) -> Sub msg
port receiveAnswer : (AnswerData -> msg) -> Sub msg

-- Signaling Confirmation Ports
port offerSent : (OperationResult -> msg) -> Sub msg
port offerSendError : (String -> msg) -> Sub msg
port answerSent : (OperationResult -> msg) -> Sub msg
port answerSendError : (String -> msg) -> Sub msg
port candidateSent : (OperationResult -> msg) -> Sub msg
port candidateSendError : (String -> msg) -> Sub msg

-- Peer Discovery Port
port peersDiscovered : (List String -> msg) -> Sub msg

-- Types for Port Data
type alias NodeInfo = 
    { peerId : String
    , multiaddrs : List String
    }

type alias PeerInfo = 
    { peerId : String 
    }

type alias OfferData = 
    { peerId : String
    , offer : String  -- Serialized WebRTC offer
    }

type alias AnswerData = 
    { peerId : String
    , answer : String  -- Serialized WebRTC answer
    }

type alias CandidateData = 
    { peerId : String
    , candidate : String  -- Serialized ICE candidate
    }

type alias OperationResult = 
    { success : Bool
    }