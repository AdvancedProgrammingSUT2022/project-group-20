package ir.ap.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ir.ap.client.components.map.panel.CurrentResearchView;
import ir.ap.client.components.map.panel.UnitActionsView;
import ir.ap.client.components.map.panel.UnitInfoView;
import ir.ap.client.components.map.MapView;
import ir.ap.client.components.map.serializers.CivilizationSerializer;
import ir.ap.client.components.map.serializers.TechnologySerializer;
import ir.ap.client.components.map.serializers.TileSerializer;
import ir.ap.client.components.map.serializers.UnitSerializer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

public class GameView extends View {

    @FXML
    private BorderPane root;

    @FXML
    private AnchorPane mapPart;

    @FXML
    private AnchorPane infoPanel;

    @FXML
    private Label goldLabel;

    @FXML
    private Label happinessLabel;

    @FXML
    private Label scienceLabel;

    @FXML
    private Label turnLabel;

    @FXML
    private Label yearLabel;

    private AnchorPane currentResearchRoot;
    private Button researchPanel;
    private Button unitsPanel;
    private Button citiesPanel;
    private Button demographicsPanel;
    private Button notificationPanel;
    private Button nextTurn;
    private static AnchorPane unitInfoRoot;
    private static AnchorPane unitActionButtonsRoot;

    private ScrollPane scrollMap;

    private AnchorPane map;
    private static MapView mapView;
    
    private static AnchorPane mapPart2;
    
    private static GameView gameView;

    public void initialize() throws IOException {
        mapPart2 = mapPart;
        initializeMap();
        scrollMap = new ScrollPane(map);
        scrollMap.setMaxWidth(App.SCREEN_WIDTH);
        scrollMap.setMaxHeight(App.SCREEN_HEIGHT-infoPanel.getPrefHeight());
        // scrollMap.setLayoutX(value);
        // scrollMap.setLayoutY(value);
        scrollMap.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollMap.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Platform.runLater(() -> {
            scrollMap.requestFocus();
        });
        mapPart.getChildren().add(scrollMap);
        mapPart.getChildren().add(makeNextTurnButton());
        makeCurrentResearchPanel();
        makeInfoButtons();
        makeNotificationsButton();
        gameView = this;
        updateStatusPart();
    }

    private void makeNotificationsButton(){
        if( notificationPanel != null )mapPart.getChildren().remove(notificationPanel);
        notificationPanel = new Button("Notifications");
        notificationPanel.getStyleClass().add("notificationButton");
        notificationPanel.setLayoutX(898);
        notificationPanel.setLayoutY(14);
        notificationPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showNotificationPanel();
            }
        });
        mapPart.getChildren().add(notificationPanel);
    }

    private void makeInfoButtons(){
        if( researchPanel != null )mapPart.getChildren().remove(researchPanel);
        if( unitsPanel != null )mapPart.getChildren().remove(unitsPanel);
        if( citiesPanel != null )mapPart.getChildren().remove(citiesPanel);
        if( demographicsPanel != null )mapPart.getChildren().remove(demographicsPanel);
        researchPanel = new Button("Researches");
        unitsPanel = new Button("Units");
        citiesPanel = new Button("Cities");
        demographicsPanel = new Button("Demographics");
        Stream.of(researchPanel, unitsPanel, citiesPanel, demographicsPanel).forEach( button -> 
        button.getStyleClass().add("gameButton"));
        researchPanel.setPrefWidth(130);
        researchPanel.setPrefHeight(24);
        researchPanel.setLayoutX(14);
        researchPanel.setLayoutY(153);
        researchPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showTechnologyInfoPanel();
            }            
        });
        unitsPanel.setPrefWidth(130);
        unitsPanel.setPrefHeight(24);
        unitsPanel.setLayoutX(14);
        unitsPanel.setLayoutY(180);
        unitsPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showUnitsInfoPanel();
            }            
        });
        citiesPanel.setPrefWidth(130);
        citiesPanel.setPrefHeight(24);
        citiesPanel.setLayoutX(14);
        citiesPanel.setLayoutY(207);
        citiesPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showCitiesInfoPanel();
            }            
        });
        demographicsPanel.setPrefWidth(130);
        demographicsPanel.setPrefHeight(24);
        demographicsPanel.setLayoutX(14);
        demographicsPanel.setLayoutY(234);
        demographicsPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showDemographicsInfoPanel();
            }            
        });
        mapPart.getChildren().addAll(researchPanel, unitsPanel, citiesPanel, demographicsPanel);
    }

    private void makeCurrentResearchPanel() throws IOException{
        if( currentResearchRoot != null )mapPart.getChildren().remove(currentResearchRoot);
        JsonObject currentResearch = send("civGetCurrentResearch", currentUsername);
        if(responseOk(currentResearch)){
            TechnologySerializer technology = GSON.fromJson(currentResearch.get("technology"), TechnologySerializer.class);
            FXMLLoader fxmlLoader = new FXMLLoader(GameView.class.getResource("fxml/components/map/panel/currentResearch-view.fxml"));
            currentResearchRoot = fxmlLoader.load();
            CurrentResearchView currentResearchView = fxmlLoader.getController();
            currentResearchView.setLabel(technology.getName() + "(" + technology.getTurnsLeftForFinish() + ")");
            currentResearchView.setImage(new Image(GameView.class.getResource("png/technology/" + technology.getName().toLowerCase() + ".png").toExternalForm()));
            currentResearchRoot.setLayoutX(0);
            currentResearchRoot.setLayoutY(0);
            mapPart.getChildren().add(currentResearchRoot);
        }
    }

    private Button makeNextTurnButton(){
        if( nextTurn != null )mapPart.getChildren().remove(nextTurn);
        nextTurn = new Button("NEXT TURN");
        nextTurn.getStyleClass().add("nextTurnButton");
        nextTurn.setLayoutX(811);
        nextTurn.setLayoutY(508);
        nextTurn.setPrefWidth(200);
        nextTurn.setPrefHeight(24); 
        if( !mapView.isAllUnitsGetAction() ){
            nextTurn.setText("Something Missed");
        }else{
            nextTurn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    nextTurn();
                }            
            });
        }
        return nextTurn;    
    }

    private void initializeMap() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GameView.class.getResource("fxml/components/map/map-view.fxml"));
        map = fxmlLoader.load();
        mapView = fxmlLoader.getController();
    }

    public void updateStatusPart(){
        // System.out.println(turnLabel);
        // System.out.println(yearLabel);
        JsonObject jsonObject = send("getCivilizationByUsername", currentUsername);
        if( responseOk(jsonObject) ){
            CivilizationSerializer civ = GSON.fromJson(jsonObject.get("civ"), CivilizationSerializer.class);
            happinessLabel.setText(""+civ.getHappiness());
            goldLabel.setText(""+civ.getGold());
            scienceLabel.setText(""+civ.getScience());
        }
        int year = GSON.fromJson(send("getYear").get("year"), int.class);
        int turn = GSON.fromJson(send("getTurn").get("turn"), int.class);
        if( year < 0 ){
            yearLabel.setText((year*(-1)) + " BC");
        }else{
            yearLabel.setText(year + " AC");
        }
        turnLabel.setText("Turn: " + turn);
    }

    public String lowerCaseString(String s1){
        String s2 = s1.toLowerCase();
        String s3 = Character.toUpperCase(s2.charAt(0)) + s2.substring(1);
        return s3;
    }

    public static void updateGame(){
        mapView.showCurrentMap();
        gameView.mapPart.getChildren().add(gameView.makeNextTurnButton());
        try {
            gameView.makeCurrentResearchPanel();            
        } catch (Exception e) {
            e.printStackTrace();
        }
        gameView.updateStatusPart();
        gameView.removeUnitInfoPanel();   
    }

    private void nextTurn(){
        removeUnitInfoPanel();
        updateGame();
        //TODO: network and waiting for others
    }

    private void showTechnologyInfoPanel() {
        ArrayList<String> improvementsNames = new ArrayList<String>();
        ArrayList<String> resourcesNames = new ArrayList<String>();
        ArrayList<String> unitTypesNames = new ArrayList<String>();
        ArrayList<String> buildingTypesNames = new ArrayList<String>();
        ArrayList<String> unitActionsNames = new ArrayList<String>();
        ArrayList<String> technologiesNames = new ArrayList<String>();

        JsonObject jsonObject = send("infoResearch", currentUsername);
        if (responseOk(jsonObject) == false) return;

        for (int j = 0; j < 6; j++) {
            JsonElement jsonElement = jsonObject.getAsJsonArray("objectsUnlocksSeparated").get(j);
            JsonArray jsonArray = GSON.fromJson(jsonElement, JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject1 = GSON.fromJson(jsonArray.get(i), JsonObject.class);
                String name = GSON.fromJson(jsonObject1.get("name"), String.class);
                if (j == 0) improvementsNames.add(name);
                if (j == 1) resourcesNames.add(name);
                if (j == 2) unitTypesNames.add(name);
                if (j == 3) buildingTypesNames.add(name);
                if (j == 4) unitActionsNames.add(name);
                if (j == 5) technologiesNames.add(name);
            }
        }
    }

    private void showUnitsInfoPanel(){
        JsonObject jsonObject = send("infoUnits", currentUsername);
        if(responseOk(jsonObject) == false) return;

        ArrayList<Integer> unitNames = new ArrayList<Integer>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("units");
        for(int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject1 = GSON.fromJson(jsonArray.get(i), JsonObject.class);
            unitNames.add(GSON.fromJson(jsonObject1.get("name"),Integer.class));
        }
    }

    private void showCitiesInfoPanel(){
        JsonObject jsonObject = send("infoCities", currentUsername);
        if(responseOk(jsonObject) == false) return;

        ArrayList<Integer> cityNames = new ArrayList<Integer>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("cities");
        for(int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject1 = GSON.fromJson(jsonArray.get(i), JsonObject.class);
            cityNames.add(GSON.fromJson(jsonObject1.get("name"),Integer.class));
        }
    }

    private void showDemographicsInfoPanel(){
        JsonObject jsonObject = send("infoDemographics", currentUsername);
        if(responseOk(jsonObject) == false) return;
        String civilizationName ;
        JsonObject jsonObject1 = GSON.fromJson(jsonObject.get("demographics"), JsonObject.class);
        civilizationName = GSON.fromJson(jsonObject1.get("name"),String.class);
    }

    private void showMilitaryInfo(){
        // can go into this panel from unitsInfoPanel
        JsonObject jsonObject = send("infoMilitary", currentUsername);
        if(responseOk(jsonObject) == false) return;

        ArrayList<String> civilizationUnitNames = new ArrayList<String>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("military");
        for(int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject1 = GSON.fromJson(jsonArray.get(i), JsonObject.class);
            civilizationUnitNames.add(GSON.fromJson(jsonObject1.get("name"),String.class));
        }
    }

    private void showEconomicInfo(){
        // can go into this panel from citiesInfoPanel
        JsonObject jsonObject = send("infoEconomic", currentUsername);
        if(responseOk(jsonObject) == false) return;

        ArrayList<String> cityNames = new ArrayList<String>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("economic");
        for(int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject1 = GSON.fromJson(jsonArray.get(i), JsonObject.class);
            cityNames.add(GSON.fromJson(jsonObject1.get("name"),String.class));
        }
        // bayad az unitsInfoPanel behesh berim
    }
    private void showNotificationPanel(){
        JsonObject jsonObject = send("infoNotifications", currentUsername);
        if(responseOk(jsonObject) == false) return;

        ArrayList<String> notifications = new ArrayList<String>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("notifications");
        for(int i = 0; i < jsonArray.size(); i++)
        {
            String notification = GSON.fromJson(jsonArray.get(i), String.class);
            notifications.add(notification);
        }
    }

    public void showSettingPanel(){
        
    }

    public void showMenuPanel(){

    }

    public static void showCityProductConstructionPanel(){
        JsonArray jsonArray = new JsonArray();
        for(int i = 0 ; i < jsonArray.size(); i ++){
            
        }
    }

    public static void showUnitInfoPanel(UnitSerializer unitSerializer) throws IOException{
        FXMLLoader fxmlLoader1 = new FXMLLoader(GameView.class.getResource("fxml/components/map/panel/unitInfo-view.fxml"));
        FXMLLoader fxmlLoader2 = new FXMLLoader(GameView.class.getResource("fxml/components/map/panel/unitActionButtons-view.fxml"));
        unitInfoRoot = fxmlLoader1.load();
        UnitInfoView unitInfoController = fxmlLoader1.getController();
        unitActionButtonsRoot = fxmlLoader2.load();
        UnitActionsView unitActionButtonsController = fxmlLoader2.getController();
        unitInfoController.setUnitSerializer(unitSerializer);
        unitInfoRoot.setLayoutX(0);
        unitInfoRoot.setLayoutY(431);   
        unitActionButtonsController.setUnit(unitSerializer, currentUsername);
        unitActionButtonsRoot.setLayoutX(0);
        unitActionButtonsRoot.setLayoutY(260);
        mapPart2.getChildren().addAll(unitInfoRoot, unitActionButtonsRoot);
        if( unitSerializer.isCombat() ){
            send("selectCombatUnit", currentUsername, unitSerializer.getTileId());
        }else{
            send("selectNonCombatUnit", currentUsername, unitSerializer.getTileId());
        }
    }

    public static void removeUnitInfoPanel(){
        if(unitInfoRoot != null)gameView.mapPart.getChildren().remove(unitInfoRoot);
        if(unitActionButtonsRoot != null)gameView.mapPart.getChildren().remove(unitActionButtonsRoot);
    }

    public static boolean unitAction(String methodName,  Object... params){
        JsonObject response = send(methodName, params);
        return responseOk(response);
    }

    public static String getEra(){
        JsonObject jsonObject = send("getEra");
        if(!responseOk(jsonObject))return null;
        return GSON.fromJson(jsonObject.get("era"), String.class);
    }

    public static boolean tileHasRoadOrRailRoad(int tileId){
        JsonObject jsonObject = send("hasRoadOrRailRoad", currentUsername, tileId);
        if( !responseOk(jsonObject) ){
            return false;
        }
        return GSON.fromJson(jsonObject.get("hasRoad"), boolean.class);
    }

    public static boolean tileCanBuildImprovement(int tileId, int impId){
        JsonObject jsonObject = send("canBuildImprovement", currentUsername, tileId, impId);
        if( !responseOk(jsonObject) ){
            return false;
        }
        return GSON.fromJson(jsonObject.get("canBuild"), boolean.class);
    }

    public static int getTerrainFeature(int tileId){
        JsonObject jsonObject = send("getTerrainFeatureByTile", currentUsername, tileId);
        if( !responseOk(jsonObject) ){
            return -1;
        }
        return GSON.fromJson(jsonObject.get("terrainFeature"), int.class);
    }

    public static TileSerializer getTileById(int tileId){
        JsonObject jsonObject = send("getTileById", currentUsername, tileId);
        if( !responseOk(jsonObject) )return null;
        return GSON.fromJson(jsonObject.get("tile"), TileSerializer.class);
    }
}
