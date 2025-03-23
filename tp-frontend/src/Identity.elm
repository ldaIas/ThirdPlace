module Identity exposing (Model, Msg(..), init, subscriptions, update)

import Json.Decode exposing (Decoder, field, string)
import Utils.Ports


type alias Model =
    { did : Maybe String -- did:key:z[ed25519 public key]
    , privKey : Maybe String
    , pubKey : Maybe String -- ed25519 public key
    , loginAuthenticated : Maybe Bool -- When logging in with a DID
    }


type Msg
    = RequestDID
    | DIDGenerated String String String -- (did, privKey, pubKey)
    | DIDAuthenticated Bool -- When logging in with a DID


init : ( Model, Cmd Msg )
init =
    ( { did = Nothing, privKey = Nothing, pubKey = Nothing, loginAuthenticated = Nothing }
    , Cmd.none
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        RequestDID ->
            ( model, Utils.Ports.generateDID () )

        DIDGenerated did privKey pubKey ->
            ( { model | did = Just did, privKey = Just privKey, pubKey = Just pubKey }
            , Cmd.none
            )

        DIDAuthenticated authed ->
            ( { model | loginAuthenticated = Just authed }, Cmd.none )


didDecoder : Decoder Msg
didDecoder =
    Json.Decode.map3 DIDGenerated
        (field "did" string)
        (field "privKey" string)
        (field "pubKey" string)


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ Utils.Ports.didGenerated (\json -> Json.Decode.decodeValue didDecoder json |> Result.withDefault (DIDGenerated "err" "err" "err"))
        , Utils.Ports.authenticationResult (\result -> DIDAuthenticated result)
        ]
