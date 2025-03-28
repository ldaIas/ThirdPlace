module JSPorts.WebRTC.WebRTCHandler exposing ( Model
    , Msg(..)
    , init
    , update
    , subscriptions
    )

import JSPorts.WebRTC.WebRTCPorts as WebRTC
import Json.Decode as Decode
import Json.Encode as Encode

-- Model represents the state of the P2P network connection
type alias Model =
    { nodeId : Maybe String
    , connectedPeers : List String
    , pendingConnections : List String
    , networkError : Maybe String
    , connectionStatus : ConnectionStatus
    }

-- Different states of network connection
type ConnectionStatus
    = Disconnected
    | Initializing
    | Connected
    | Error

-- Messages for network operations
type Msg
    -- Network Initialization
    = InitializeNetwork
    | NodeInitialized WebRTC.NodeInfo
    | NodeInitializationFailed String

    -- Peer Connection Management
    | PeerConnected WebRTC.PeerInfo
    | PeerDisconnected WebRTC.PeerInfo

    -- WebRTC Signaling Messages
    | SendWebRTCOffer String  -- Peer ID to send offer to
    | OfferSent WebRTC.OperationResult
    | ReceiveWebRTCOffer WebRTC.OfferData
    | SendWebRTCAnswer String  -- Peer ID to send answer to
    | AnswerSent WebRTC.OperationResult
    | ReceiveWebRTCAnswer WebRTC.AnswerData

    -- ICE Candidate Handling
    | SendICECandidate WebRTC.CandidateData
    | CandidateSent WebRTC.OperationResult

    -- Peer Discovery
    | PeersDiscovered (List String)

    -- Error Handling
    | HandleNetworkError String

-- Initial model state
init : () -> (Model, Cmd Msg)
init _ =
    ( { nodeId = Nothing
      , connectedPeers = []
      , pendingConnections = []
      , networkError = Nothing
      , connectionStatus = Disconnected
      }
    , Cmd.none
    )

-- Update function to handle network-related messages
update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        InitializeNetwork ->
            ( { model | connectionStatus = Initializing }
            , WebRTC.createConnection ()
            )

        NodeInitialized nodeInfo ->
            ( { model 
                | nodeId = Just nodeInfo.peerId
                , connectionStatus = Connected
                , networkError = Nothing
              }
            , Cmd.none
            )

        NodeInitializationFailed errorMsg ->
            ( { model 
                | connectionStatus = Error
                , networkError = Just errorMsg
              }
            , Cmd.none
            )

        PeerConnected peerInfo ->
            ( { model 
                | connectedPeers = peerInfo.peerId :: model.connectedPeers
                , pendingConnections = 
                    List.filter (\p -> p /= peerInfo.peerId) model.pendingConnections
              }
            , Cmd.none
            )

        PeerDisconnected peerInfo ->
            ( { model 
                | connectedPeers = 
                    List.filter (\p -> p /= peerInfo.peerId) model.connectedPeers
              }
            , Cmd.none
            )

        SendWebRTCOffer peerId ->
            ( model
            , WebRTC.sendOffer 
                { peerId = peerId
                , offer = encodeWebRTCOffer peerId
                }
            )

        OfferSent success ->
            ( model
            , Cmd.none
            )

        ReceiveWebRTCOffer offerData ->
            ( model
            , handleIncomingOffer offerData
            )

        SendWebRTCAnswer peerId ->
            ( model
            , WebRTC.sendAnswer 
                { peerId = peerId
                , answer = encodeWebRTCAnswer peerId
                }
            )

        AnswerSent success ->
            ( model
            , Cmd.none
            )

        ReceiveWebRTCAnswer answerData ->
            ( model
            , handleIncomingAnswer answerData
            )

        SendICECandidate candidateData ->
            ( model
            , WebRTC.sendCandidate candidateData
            )

        CandidateSent success ->
            ( model
            , Cmd.none
            )

        PeersDiscovered peerIds ->
            ( { model 
                | pendingConnections = 
                    List.filter 
                        (\peerId -> not (List.member peerId model.connectedPeers)) 
                        peerIds
              }
            , Cmd.none
            )

        HandleNetworkError errorMsg ->
            ( { model 
                | networkError = Just errorMsg
                , connectionStatus = Error
              }
            , Cmd.none
            )

-- Subscriptions to network events
subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ WebRTC.nodeInitialized NodeInitialized
        , WebRTC.nodeInitializationError NodeInitializationFailed
        , WebRTC.peerConnected PeerConnected
        , WebRTC.peerDisconnected PeerDisconnected
        , WebRTC.receiveOffer ReceiveWebRTCOffer
        , WebRTC.receiveAnswer ReceiveWebRTCAnswer
        , WebRTC.offerSent OfferSent
        , WebRTC.answerSent AnswerSent
        , WebRTC.candidateSent CandidateSent
        , WebRTC.peersDiscovered PeersDiscovered
        ]

-- Helper functions for encoding WebRTC-related data
encodeWebRTCOffer : String -> String
encodeWebRTCOffer peerId =
    -- Implement your WebRTC offer encoding logic
    -- This is a placeholder - you'll need to replace with actual implementation
    Encode.encode 0 
        (Encode.object
            [ ("peerId", Encode.string peerId)
            , ("type", Encode.string "offer")
            ]
        )

encodeWebRTCAnswer : String -> String
encodeWebRTCAnswer peerId =
    -- Implement your WebRTC answer encoding logic
    -- This is a placeholder - you'll need to replace with actual implementation
    Encode.encode 0 
        (Encode.object
            [ ("peerId", Encode.string peerId)
            , ("type", Encode.string "answer")
            ]
        )

-- Handle incoming WebRTC offer
handleIncomingOffer : WebRTC.OfferData -> Cmd Msg
handleIncomingOffer offerData =
    -- Implement logic for processing incoming WebRTC offer
    -- This might involve sending an answer, updating UI, etc.
    Cmd.none

-- Handle incoming WebRTC answer
handleIncomingAnswer : WebRTC.AnswerData -> Cmd Msg
handleIncomingAnswer answerData =
    -- Implement logic for processing incoming WebRTC answer
    -- This might involve finalizing connection, updating UI, etc.
    Cmd.none