module Posts exposing (Model, Msg(..), Post, RSVP, init, update, view, loadPosts)

import Html exposing (Html, button, div, form, h2, input, option, p, select, span, text, textarea)
import Html.Attributes exposing (class, placeholder, type_, value)
import Html.Events exposing (onClick, onInput, onSubmit, preventDefaultOn)
import Http
import Json.Decode as Decode
import Json.Encode as Encode


type alias Post =
    { id : String
    , title : String
    , author : String
    , description : Maybe String
    , createdAt : String
    , endDate : String
    , groupSize : Int
    , tags : List String
    , location : Maybe String
    , proposedTime : String
    , isDateActivity : Bool
    , status : String
    , category : String
    }


type alias RSVP =
    { id : String
    , userId : String
    , postId : String
    , status : String
    , createdAt : String
    }


type alias NewPost =
    { title : String
    , description : String
    , location : String
    , groupSize : String
    , category : String
    }


type alias Model =
    { posts : List Post
    , showCreateForm : Bool
    , newPost : NewPost
    , error : Maybe String
    , showRSVPConfirm : Maybe String
    , userRSVPs : List RSVP
    , showMyRSVPs : Bool
    , userPosts : List Post
    , showMyPosts : Bool
    , postRSVPs : List ( String, List RSVP )
    , expandedPost : Maybe String
    }


type Msg
    = PostsLoaded (Result Http.Error (List Post))
    | PostCreated (Result Http.Error ())
    | RefreshPosts
    | ShowCreateForm
    | HideCreateForm
    | UpdateTitle String
    | UpdateDescription String
    | UpdateLocation String
    | UpdateGroupSize String
    | UpdateCategory String
    | SubmitPost
    | ShowRSVPConfirm String
    | HideRSVPConfirm
    | ConfirmRSVP String
    | RSVPCreated (Result Http.Error ())
    | ShowMyRSVPs
    | HideMyRSVPs
    | LoadUserRSVPs
    | UserRSVPsLoaded (Result Http.Error (List RSVP))
    | DeleteRSVP String
    | RSVPDeleted (Result Http.Error ())
    | ShowMyPosts
    | HideMyPosts
    | LoadUserPosts
    | UserPostsLoaded (Result Http.Error (List Post))
    | LoadPostRSVPs String
    | PostRSVPsLoaded String (Result Http.Error (List RSVP))
    | TogglePostExpansion String
    | RespondToRSVP String String
    | RSVPResponseSent (Result Http.Error ())


init : Model
init =
    { posts = []
    , showCreateForm = False
    , newPost = emptyNewPost
    , error = Nothing
    , showRSVPConfirm = Nothing
    , userRSVPs = []
    , showMyRSVPs = False
    , userPosts = []
    , showMyPosts = False
    , postRSVPs = []
    , expandedPost = Nothing
    }


emptyNewPost : NewPost
emptyNewPost =
    { title = ""
    , description = ""
    , location = ""
    , groupSize = "2"
    , category = "coffee"
    }


loadPosts : Cmd Msg
loadPosts =
    Http.get
        { url = "http://localhost:8080/api/Posts:getAll"
        , expect = Http.expectJson PostsLoaded postsDecoder
        }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        PostsLoaded result ->
            case result of
                Ok posts ->
                    ( { model | posts = posts, error = Nothing }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "Error loading posts" err
                    in
                    ( { model | error = Just "Failed to load posts. Please check if the server is running." }, Cmd.none )

        PostCreated result ->
            case result of
                Ok _ ->
                    ( { model | showCreateForm = False, newPost = emptyNewPost }, loadPosts )

                Err err ->
                    let
                        _ =
                            Debug.log "Error creating post" err
                    in
                    ( { model | error = Just "Failed to create post. Please try again." }, Cmd.none )

        RefreshPosts ->
            ( { model | error = Nothing }, loadPosts )

        ShowCreateForm ->
            ( { model | showCreateForm = True }, Cmd.none )

        HideCreateForm ->
            ( { model | showCreateForm = False, newPost = emptyNewPost }, Cmd.none )

        UpdateTitle title ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | title = title } }, Cmd.none )

        UpdateDescription description ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | description = description } }, Cmd.none )

        UpdateLocation location ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | location = location } }, Cmd.none )

        UpdateGroupSize groupSize ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | groupSize = groupSize } }, Cmd.none )

        UpdateCategory category ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | category = category } }, Cmd.none )

        SubmitPost ->
            if String.isEmpty (String.trim model.newPost.title) then
                ( model, Cmd.none )
            else
                ( model, createPost model.newPost )

        ShowRSVPConfirm postId ->
            ( { model | showRSVPConfirm = Just postId }, Cmd.none )

        HideRSVPConfirm ->
            ( { model | showRSVPConfirm = Nothing }, Cmd.none )

        ConfirmRSVP postId ->
            ( { model | showRSVPConfirm = Nothing }, createRSVP postId )

        RSVPCreated result ->
            case result of
                Ok _ ->
                    ( model, loadPosts )

                Err err ->
                    let
                        _ =
                            Debug.log "Error creating RSVP" err
                    in
                    ( { model | error = Just "Failed to create RSVP. Please try again." }, Cmd.none )

        ShowMyRSVPs ->
            ( { model | showMyRSVPs = True, showMyPosts = False }, loadUserRSVPs )

        HideMyRSVPs ->
            ( { model | showMyRSVPs = False, showMyPosts = False }, Cmd.none )

        LoadUserRSVPs ->
            ( model, loadUserRSVPs )

        UserRSVPsLoaded result ->
            case result of
                Ok rsvps ->
                    ( { model | userRSVPs = rsvps, error = Nothing }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "Error loading user RSVPs" err
                    in
                    ( { model | error = Just "Failed to load RSVPs. Please try again." }, Cmd.none )

        DeleteRSVP rsvpId ->
            ( model, deleteRSVP rsvpId )

        RSVPDeleted result ->
            case result of
                Ok _ ->
                    ( model, loadUserRSVPs )

                Err err ->
                    let
                        _ =
                            Debug.log "Error deleting RSVP" err
                    in
                    ( { model | error = Just "Failed to delete RSVP. Please try again." }, Cmd.none )

        ShowMyPosts ->
            ( { model | showMyPosts = True, showMyRSVPs = False }, loadUserPosts )

        HideMyPosts ->
            ( { model | showMyPosts = False, showMyRSVPs = False }, Cmd.none )

        LoadUserPosts ->
            ( model, loadUserPosts )

        UserPostsLoaded result ->
            case result of
                Ok posts ->
                    let
                        loadRSVPsCmd = 
                            posts
                                |> List.map (\post -> loadPostRSVPs post.id)
                                |> Cmd.batch
                    in
                    ( { model | userPosts = posts, error = Nothing }, loadRSVPsCmd )

                Err err ->
                    let
                        _ =
                            Debug.log "Error loading user posts" err
                    in
                    ( { model | error = Just "Failed to load posts. Please try again." }, Cmd.none )

        LoadPostRSVPs postId ->
            ( model, loadPostRSVPs postId )

        PostRSVPsLoaded postId result ->
            case result of
                Ok rsvps ->
                    let
                        updatedPostRSVPs =
                            ( postId, rsvps ) :: List.filter (\( id, _ ) -> id /= postId) model.postRSVPs
                    in
                    ( { model | postRSVPs = updatedPostRSVPs, error = Nothing }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "Error loading post RSVPs" err
                    in
                    ( { model | error = Just "Failed to load RSVPs. Please try again." }, Cmd.none )

        TogglePostExpansion postId ->
            let
                newExpandedPost =
                    if model.expandedPost == Just postId then
                        Nothing
                    else
                        Just postId
                
                cmd =
                    if newExpandedPost == Just postId then
                        loadPostRSVPs postId
                    else
                        Cmd.none
            in
            ( { model | expandedPost = newExpandedPost }, cmd )

        RespondToRSVP rsvpId response ->
            ( model, respondToRSVP rsvpId response )

        RSVPResponseSent result ->
            case result of
                Ok _ ->
                    case model.expandedPost of
                        Just postId ->
                            ( model, loadPostRSVPs postId )
                        Nothing ->
                            ( model, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "Error responding to RSVP" err
                    in
                    ( { model | error = Just "Failed to respond to RSVP. Please try again." }, Cmd.none )


view : Model -> Html Msg
view model =
    div [ class "posts-container" ]
        [ div [ class "posts-header" ]
            [ h2 [ class "posts-title" ] [ text "Recent Activities" ]
            , div [ class "header-buttons" ]
                [ button [ class "my-posts-button", onClick ShowMyPosts ] [ text "My Posts" ]
                , button [ class "my-rsvps-button", onClick ShowMyRSVPs ] [ text "My RSVPs" ]
                , button [ class "refresh-button", onClick RefreshPosts ] [ text "Refresh" ]
                ]
            ]
        , case model.error of
            Just errorMsg ->
                div [ class "error-message" ] [ text errorMsg ]

            Nothing ->
                if model.showMyRSVPs then
                    viewMyRSVPs model.userRSVPs
                else if model.showMyPosts then
                    viewMyPosts model model.userPosts
                else
                    div [ class "posts-list" ] (List.map viewPost model.posts)
        , if model.showCreateForm then
            viewCreateForm model.newPost
          else if model.showMyRSVPs || model.showMyPosts then
            text ""
          else
            button [ class "create-button", onClick ShowCreateForm ] [ text "+" ]
        , case model.showRSVPConfirm of
            Just postId ->
                viewRSVPConfirmDialog postId
            Nothing ->
                text ""
        ]


viewPost : Post -> Html Msg
viewPost post =
    div [ class "post-card" ]
        [ div [ class "post-header" ]
            [ div [ class "post-title" ] [ text post.title ]
            , div [ class "post-meta" ]
                [ span [ class "post-author" ] [ text ("by " ++ post.author) ]
                , span [ class "post-category" ] [ text post.category ]
                ]
            ]
        , case post.description of
            Just desc ->
                p [ class "post-description" ] [ text desc ]

            Nothing ->
                text ""
        , div [ class "post-details" ]
            [ div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "When:" ]
                , span [ class "detail-value" ] [ text post.proposedTime ]
                ]
            , case post.location of
                Just loc ->
                    div [ class "post-detail" ]
                        [ span [ class "detail-label" ] [ text "Where:" ]
                        , span [ class "detail-value" ] [ text loc ]
                        ]

                Nothing ->
                    text ""
            , div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "Group size:" ]
                , span [ class "detail-value" ] [ text (String.fromInt post.groupSize ++ " people") ]
                ]
            ]
        , if List.isEmpty post.tags then
            text ""

          else
            div [ class "post-tags" ] (List.map viewTag post.tags)
        , div [ class "post-actions" ]
            [ if post.author /= "current_user" then
                button [ class "rsvp-button", onClick (ShowRSVPConfirm post.id) ] [ text "RSVP" ]
              else
                text ""
            ]
        ]


viewTag : String -> Html Msg
viewTag tag =
    span [ class "tag" ] [ text tag ]


createPost : NewPost -> Cmd Msg
createPost newPost =
    Http.post
        { url = "http://localhost:8080/api/Posts:create"
        , body = Http.jsonBody (encodeNewPost newPost)
        , expect = Http.expectWhatever PostCreated
        }


createRSVP : String -> Cmd Msg
createRSVP postId =
    Http.post
        { url = "http://localhost:8080/api/RSVPs:create"
        , body = Http.jsonBody (encodeCreateRSVP postId)
        , expect = Http.expectWhatever RSVPCreated
        }


loadUserRSVPs : Cmd Msg
loadUserRSVPs =
    Http.get
        { url = "http://localhost:8080/api/RSVPs:getByUser/current_user"
        , expect = Http.expectJson UserRSVPsLoaded rsvpsDecoder
        }


deleteRSVP : String -> Cmd Msg
deleteRSVP rsvpId =
    Http.request
        { method = "DELETE"
        , headers = []
        , url = "http://localhost:8080/api/RSVPs:delete/" ++ rsvpId
        , body = Http.emptyBody
        , expect = Http.expectWhatever RSVPDeleted
        , timeout = Nothing
        , tracker = Nothing
        }


loadUserPosts : Cmd Msg
loadUserPosts =
    Http.get
        { url = "http://localhost:8080/api/Posts:getByUser/current_user"
        , expect = Http.expectJson UserPostsLoaded postsDecoder
        }


loadPostRSVPs : String -> Cmd Msg
loadPostRSVPs postId =
    Http.get
        { url = "http://localhost:8080/api/RSVPs:getByPost/" ++ postId
        , expect = Http.expectJson (PostRSVPsLoaded postId) rsvpsDecoder
        }


respondToRSVP : String -> String -> Cmd Msg
respondToRSVP rsvpId response =
    Http.request
        { method = "PUT"
        , headers = []
        , url = "http://localhost:8080/api/RSVPs:respond"
        , body = Http.jsonBody (encodeRSVPResponse rsvpId response)
        , expect = Http.expectWhatever RSVPResponseSent
        , timeout = Nothing
        , tracker = Nothing
        }


encodeNewPost : NewPost -> Encode.Value
encodeNewPost newPost =
    Encode.object
        [ ( "title", Encode.string newPost.title )
        , ( "author", Encode.string "current_user" )
        , ( "description", Encode.string newPost.description )
        , ( "endDate", Encode.string "2025-01-28T23:59:00Z" )
        , ( "groupSize", Encode.int (String.toInt newPost.groupSize |> Maybe.withDefault 2) )
        , ( "tags", Encode.list Encode.string [] )
        , ( "location", Encode.string newPost.location )
        , ( "latitude", Encode.float 37.7749 )
        , ( "longitude", Encode.float -122.4194 )
        , ( "proposedTime", Encode.string "2025-01-28T15:00:00Z" )
        , ( "genderBalance", Encode.string "any" )
        , ( "category", Encode.string newPost.category )
        ]


encodeCreateRSVP : String -> Encode.Value
encodeCreateRSVP postId =
    Encode.object
        [ ( "userId", Encode.string "current_user" )
        , ( "postId", Encode.string postId )
        ]


encodeRSVPResponse : String -> String -> Encode.Value
encodeRSVPResponse rsvpId response =
    Encode.object
        [ ( "rsvpId", Encode.string rsvpId )
        , ( "response", Encode.string response )
        ]


postsDecoder : Decode.Decoder (List Post)
postsDecoder =
    Decode.field "posts" (Decode.list postDecoder)


rsvpsDecoder : Decode.Decoder (List RSVP)
rsvpsDecoder =
    Decode.field "rsvps" (Decode.list rsvpDecoder)


rsvpDecoder : Decode.Decoder RSVP
rsvpDecoder =
    Decode.succeed RSVP
        |> andMap (Decode.field "id" Decode.string)
        |> andMap (Decode.field "userId" Decode.string)
        |> andMap (Decode.field "postId" Decode.string)
        |> andMap (Decode.field "status" Decode.string)
        |> andMap (Decode.field "createdAt" Decode.string)


postDecoder : Decode.Decoder Post
postDecoder =
    Decode.succeed Post
        |> andMap (Decode.field "id" Decode.string)
        |> andMap (Decode.field "title" Decode.string)
        |> andMap (Decode.field "author" Decode.string)
        |> andMap (Decode.maybe (Decode.field "description" Decode.string))
        |> andMap (Decode.field "createdAt" Decode.string)
        |> andMap (Decode.field "endDate" Decode.string)
        |> andMap (Decode.field "groupSize" Decode.int)
        |> andMap (Decode.field "tags" (Decode.list Decode.string))
        |> andMap (Decode.maybe (Decode.field "location" Decode.string))
        |> andMap (Decode.field "proposedTime" Decode.string)
        |> andMap (Decode.field "isDateActivity" Decode.bool)
        |> andMap (Decode.field "status" Decode.string)
        |> andMap (Decode.field "category" Decode.string)


andMap : Decode.Decoder a -> Decode.Decoder (a -> b) -> Decode.Decoder b
andMap =
    Decode.map2 (|>)


viewRSVPConfirmDialog : String -> Html Msg
viewRSVPConfirmDialog postId =
    div [ class "rsvp-confirm-overlay" ]
        [ div [ class "rsvp-confirm-dialog" ]
            [ h2 [ class "dialog-title" ] [ text "Confirm RSVP" ]
            , p [ class "dialog-message" ] [ text "Are you sure you want to RSVP to this activity?" ]
            , div [ class "dialog-actions" ]
                [ button [ class "cancel-btn", onClick HideRSVPConfirm ] [ text "Cancel" ]
                , button [ class "confirm-btn", onClick (ConfirmRSVP postId) ] [ text "Confirm RSVP" ]
                ]
            ]
        ]


viewMyRSVPs : List RSVP -> Html Msg
viewMyRSVPs rsvps =
    div [ class "my-rsvps" ]
        [ div [ class "my-rsvps-header" ]
            [ h2 [ class "my-rsvps-title" ] [ text "RSVPs I've Sent" ]
            , button [ class "back-button", onClick (HideMyRSVPs) ] [ text "← Back to Posts" ]
            ]
        , if List.isEmpty rsvps then
            div [ class "no-rsvps" ] [ text "You haven't sent any RSVPs yet." ]
          else
            div [ class "rsvps-list" ] (List.map viewRSVP rsvps)
        ]


viewMyPosts : Model -> List Post -> Html Msg
viewMyPosts model posts =
    div [ class "my-posts" ]
        [ div [ class "my-posts-header" ]
            [ h2 [ class "my-posts-title" ] [ text "My Posts" ]
            , button [ class "back-button", onClick HideMyPosts ] [ text "← Back to Posts" ]
            ]
        , if List.isEmpty posts then
            div [ class "no-posts" ] [ text "You haven't created any posts yet." ]
          else
            div [ class "posts-list" ] (List.map (viewMyPost model) posts)
        ]


viewMyPost : Model -> Post -> Html Msg
viewMyPost model post =
    let
        postRSVPs =
            List.filter (\( postId, _ ) -> postId == post.id) model.postRSVPs
                |> List.head
                |> Maybe.map Tuple.second
                |> Maybe.withDefault []
        
        acceptedCount =
            List.filter (\rsvp -> rsvp.status == "ACCEPTED") postRSVPs |> List.length
        
        pendingRSVPs =
            List.filter (\rsvp -> rsvp.status == "PENDING") postRSVPs
        
        isExpanded =
            model.expandedPost == Just post.id
    in
    div [ class "post-card" ]
        [ div [ class "post-header" ]
            [ div [ class "post-title" ] [ text post.title ]
            , div [ class "post-meta" ]
                [ span [ class "post-category" ] [ text post.category ]
                ]
            ]
        , case post.description of
            Just desc ->
                p [ class "post-description" ] [ text desc ]
            Nothing ->
                text ""
        , div [ class "post-details" ]
            [ div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "When:" ]
                , span [ class "detail-value" ] [ text post.proposedTime ]
                ]
            , case post.location of
                Just loc ->
                    div [ class "post-detail" ]
                        [ span [ class "detail-label" ] [ text "Where:" ]
                        , span [ class "detail-value" ] [ text loc ]
                        ]
                Nothing ->
                    text ""
            , div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "Group size:" ]
                , span [ class "detail-value" ] [ text (String.fromInt acceptedCount ++ "/" ++ String.fromInt post.groupSize ++ " people") ]
                ]
            ]
        , if acceptedCount > 0 then
            div [ class "accepted-members" ]
                [ span [ class "members-label" ] [ text "Accepted:" ]
                , div [ class "members-list" ] 
                    (List.filter (\rsvp -> rsvp.status == "ACCEPTED") postRSVPs
                        |> List.map viewAcceptedMember)
                ]
          else
            text ""
        , if List.isEmpty post.tags then
            text ""
          else
            div [ class "post-tags" ] (List.map viewTag post.tags)
        , div [ class "rsvp-section" ]
            [ button 
                [ class "rsvp-toggle"
                , onClick (TogglePostExpansion post.id)
                ] 
                [ text (String.fromInt (List.length pendingRSVPs) ++ " pending RSVPs " ++ (if isExpanded then "▲" else "▼")) ]
            , if isExpanded then
                div [ class "rsvp-list" ] (List.map (viewPostRSVP post.groupSize acceptedCount) pendingRSVPs)
              else
                text ""
            ]
        ]


viewAcceptedMember : RSVP -> Html Msg
viewAcceptedMember rsvp =
    div [ class "member-box" ] [ text rsvp.userId ]

viewPostRSVP : Int -> Int -> RSVP -> Html Msg
viewPostRSVP groupSize acceptedCount rsvp =
    let
        canAccept = acceptedCount < groupSize
    in
    div [ class "post-rsvp-card" ]
        [ div [ class "rsvp-info" ]
            [ span [ class "rsvp-user" ] [ text ("From: " ++ rsvp.userId) ]
            , span [ class "rsvp-date" ] [ text rsvp.createdAt ]
            ]
        , div [ class "rsvp-actions" ]
            [ if canAccept then
                button [ class "accept-btn", onClick (RespondToRSVP rsvp.id "ACCEPT") ] [ text "Accept" ]
              else
                button [ class "accept-btn disabled" ] [ text "Group Full" ]
            , button [ class "decline-btn", onClick (RespondToRSVP rsvp.id "DECLINE") ] [ text "Decline" ]
            ]
        ]

viewRSVP : RSVP -> Html Msg
viewRSVP rsvp =
    div [ class "rsvp-card" ]
        [ div [ class "rsvp-info" ]
            [ p [ class "rsvp-post" ] [ text ("Post ID: " ++ rsvp.postId) ]
            , p [ class "rsvp-status" ] [ text ("Status: " ++ rsvp.status) ]
            , p [ class "rsvp-date" ] [ text ("Created: " ++ rsvp.createdAt) ]
            ]
        , button [ class "delete-rsvp-btn", onClick (DeleteRSVP rsvp.id) ] [ text "Delete" ]
        ]


viewCreateForm : NewPost -> Html Msg
viewCreateForm newPost =
    div [ class "create-form-overlay" ]
        [ div [ class "create-form" ]
            [ div [ class "form-header" ]
                [ h2 [ class "form-title" ] [ text "Create Activity" ]
                , button [ class "cancel-button", onClick HideCreateForm ] [ text "×" ]
                ]
            , form [ onSubmit SubmitPost, preventDefaultOn "submit" (Decode.succeed ( SubmitPost, True )) ]
                [ div [ class "form-field" ]
                    [ input
                        [ type_ "text"
                        , placeholder "Activity title (e.g., Coffee at Blue Bottle)"
                        , value newPost.title
                        , onInput UpdateTitle
                        , class "form-input"
                        ]
                        []
                    ]
                , div [ class "form-field" ]
                    [ textarea
                        [ placeholder "Description (optional)"
                        , value newPost.description
                        , onInput UpdateDescription
                        , class "form-textarea"
                        ]
                        []
                    ]
                , div [ class "form-field" ]
                    [ input
                        [ type_ "text"
                        , placeholder "Location (e.g., Blue Bottle Coffee, 123 Main St)"
                        , value newPost.location
                        , onInput UpdateLocation
                        , class "form-input"
                        ]
                        []
                    ]
                , div [ class "form-row" ]
                    [ div [ class "form-field" ]
                        [ select
                            [ value newPost.groupSize
                            , onInput UpdateGroupSize
                            , class "form-select"
                            ]
                            [ option [ value "2" ] [ text "2 people" ]
                            , option [ value "3" ] [ text "3 people" ]
                            , option [ value "4" ] [ text "4 people" ]
                            , option [ value "5" ] [ text "5 people" ]
                            ]
                        ]
                    , div [ class "form-field" ]
                        [ select
                            [ value newPost.category
                            , onInput UpdateCategory
                            , class "form-select"
                            ]
                            [ option [ value "coffee" ] [ text "Coffee" ]
                            , option [ value "food" ] [ text "Food" ]
                            , option [ value "drinks" ] [ text "Drinks" ]
                            , option [ value "outdoor" ] [ text "Outdoor" ]
                            , option [ value "fitness" ] [ text "Fitness" ]
                            , option [ value "culture" ] [ text "Culture" ]
                            ]
                        ]
                    ]
                , div [ class "form-actions" ]
                    [ button [ type_ "button", class "cancel-btn", onClick HideCreateForm ] [ text "Cancel" ]
                    , button [ type_ "submit", class "submit-btn" ] [ text "Create Activity" ]
                    ]
                ]
            ]
        ]