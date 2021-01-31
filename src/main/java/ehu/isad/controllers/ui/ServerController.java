package ehu.isad.controllers.ui;


import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ehu.isad.controllers.db.ServerCMSDB;
import ehu.isad.model.ServerCMSModel;
import ehu.isad.utils.Url;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.sql.SQLException;


public class ServerController {
    @FXML
    private Pane pane1;

    @FXML
    private TextField textField;

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button scanBtn;

    @FXML
    private TableView<ServerCMSModel> serverTable;

    @FXML
    private TableColumn<ServerCMSModel, FontAwesomeIconView> starColumn;

    @FXML
    private TableColumn<ServerCMSModel, String> urlColumn;

    @FXML
    private TableColumn<ServerCMSModel, String> serverColumn;

    @FXML
    private TableColumn<ServerCMSModel, String> versionColumn;

    @FXML
    private TableColumn<ServerCMSModel, Date> lastUpdatedColumn;

    private final ServerCMSController serverCMSController = ServerCMSController.getInstance();
    private final ServerCMSDB servercmsdb = ServerCMSDB.getInstance();
    Url urlUtils = new Url();
    Desktop desktop = java.awt.Desktop.getDesktop();

    private static ServerController instance = new ServerController();

    private ServerController(){}

    public static ServerController getInstance() { return instance; }

    @FXML
    private MenuItem openBrowser;
    @FXML
    private MenuItem favUnFav;
    @FXML
    private MenuItem scanTwitter;
    @FXML
    private MenuItem scanReddit;
    @FXML
    private MenuItem scanTumblr;
    @FXML
    private MenuItem targetTwitter;
    @FXML
    private MenuItem targetFacebook;
    @FXML
    private MenuItem targetReddit;
    @FXML
    private MenuItem targetTumblr;

    @FXML
    void onBrowserRow(ActionEvent event) throws IOException {
        ServerCMSModel model = serverTable.getSelectionModel().getSelectedItem();
        this.openURL(model.getUrl().getText());
    }
    void openURL(String url) throws IOException {
        if(System.getProperty("os.name").toLowerCase().contains("linux")){
            Runtime.getRuntime().exec("sensible-browser " + url);
        }else{
            desktop.browse(URI.create(url));
        }
    }

    @FXML
    void onFavUnFavRow(ActionEvent event) {
        ServerCMSModel item = serverTable.getSelectionModel().getSelectedItem();
        if(item != null) {
            if (servercmsdb.isFav(item.getUrl().getText())) {
                servercmsdb.removeFromFavorites(item);
                item.setStar(0);
                if(comboBox.getValue().equals("Favorites")){
                    filterFavorites();
                    CMSController.getInstance().filterFavorites();
                } else {
                    filterAll();
                    CMSController.getInstance().filterAll();
                }
            } else {
                item.setStar(1);
                ServerCMSDB.getInstance().addToFavorites(item);
                filterAll();
                CMSController.getInstance().filterAll();
            }
        }
    }

    @FXML
    void scan(ActionEvent event) throws IOException {
        MenuItem menuitem = (MenuItem) event.getSource();
        ServerCMSModel item = serverTable.getSelectionModel().getSelectedItem();
        if (item != null){
            String url = null;
            String[] encoded = encoded(item);
            String domain = URLEncoder.encode(item.getUrl().getText().replace("/","").split(":")[1], StandardCharsets.UTF_8);
            if (menuitem.equals(scanTwitter)){
                url = "https://twitter.com/intent/tweet?text=Target%3A%20"+encoded[0]+
                        "%0AServer%3A%20"+encoded[1]+
                        "%0AServer%20version%3A%20"+encoded[2]+
                        "%0A%0AScanned%20on%20"+encoded[3]+
                        "%20with%20%40WhatWebGUI&hashtags=whatweb";
            } else if (menuitem.equals(scanReddit)){
                url = "http://www.reddit.com/submit?text=Target%3A%20"+encoded[0]+
                        "%20%20%0AServer%3A%20"+encoded[1]+
                        "%20%20%0AServer%20version%3A%20"+encoded[2]+
                        "%20%20%0A%0AScanned%20on%20"+encoded[3]+
                        "%20with%20%5B%40WhatWebGUI%5D(https%3A%2F%2Fwhatwebgui.github.io%2F)"+
                        "&title="+domain+"%20%7C%20Server%20scan%20%7C%20WhatWebGUI";
            } else if (menuitem.equals(scanTumblr)){
                url = "https://www.tumblr.com/widgets/share/tool?posttype=link&caption=Target%3A%20"+encoded[0]+
                        "%3Cbr%3EServer%3A%20"+encoded[1]+
                        "%3Cbr%3EServer%20version%3A%20"+encoded[2]+
                        "%3Cbr%3E%3Cbr%3EScanned%20on%20"+encoded[3]+
                        "%20with%20%3Ca%20href%3D%22https%3A%2F%2Fwhatwebgui.github.io%2F%22%3E%40WhatWebGUI%3C%2Fa%3E"+
                        "&tags=whatwebgui%2Cwhatweb%2C"+domain+"&canonicalUrl="+encoded[0];
            }
            assert url != null;
            if(System.getProperty("os.name").toLowerCase().contains("linux")){
                Runtime.getRuntime().exec("sensible-browser " + url);
            }else{
                java.awt.Desktop.getDesktop().browse(URI.create(url));
            }
        }
    }

    @FXML
    void target(ActionEvent event) throws IOException {
        CMSController.getItem(event, serverTable, targetTwitter, targetFacebook, targetReddit, targetTumblr, desktop);
    }

    private String[] encoded(ServerCMSModel item) {
        return serverCMSController.encoded(item,'s');
    }

    @FXML
    void onClick(ActionEvent event) throws IOException, SQLException {
        scan();
    }

    @FXML
    void onKeyPressed(KeyEvent event) throws IOException, SQLException {
        if (event.getCode().toString().equals("ENTER")) {
            scan();
        }
    }

    private void scan() throws IOException, SQLException {
        if(!textField.getText().equals("")){
            String url = urlUtils.processUrl(textField.getText());
            if(url!=null){
                processUrl(url);
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error in SERVER");
            alert.setHeaderText("Error on reading the provided URL");
            alert.setContentText("Textfield is empty");
            alert.showAndWait();
        }
    }

    void processUrl(String url){
        Thread thread = new Thread( () -> {
            try {
                Server(url);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        thread.start();
    }

    void Server(String url) throws IOException {
        textField.setText("");
        String domain = url.replace("/", "").split(":")[1];
        serverCMSController.click(domain, url);
        CMSController.getInstance().filterAll();
        filterAll();
    }

    public void filterAll(){
        FilteredList<ServerCMSModel> filteredData = new FilteredList<>(serverCMSController.getServerCMSList(), b -> true);
        filter(filteredData);
    }

    public void filterFavorites(){
        FilteredList<ServerCMSModel> filteredData = new FilteredList<>(serverCMSController.getFav(), b -> true);
        filter(filteredData);
    }

    private void filter(FilteredList<ServerCMSModel> filteredData) {
        // 2. Set the filter Predicate whenever the filter changes.
        textField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(cmsmodel -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Compare first name and last name of every person with filter text.
            String lowerCaseFilter = newValue.toLowerCase();
            if (cmsmodel.getUrl().getText().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Filter matches first name.
            } else // Does not match.
                if (cmsmodel.getServer().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                } else if(cmsmodel.getLastUpdated().toString().toLowerCase().contains(lowerCaseFilter)){
                    return true;
                }   else return cmsmodel.getVersions().toLowerCase().contains(lowerCaseFilter);
        }));
        // 3. Wrap the FilteredList in a SortedList.
        SortedList<ServerCMSModel> sortedData = new SortedList<>(filteredData);
        // 4. Bind the SortedList comparator to the TableView comparator.
        // 	  Otherwise, sorting the TableView would have no effect.
        sortedData.comparatorProperty().bind(serverTable.comparatorProperty());
        // 5. Add sorted (and filtered) data to the table.
        serverTable.setItems(sortedData);
    }

    private void style(){
        CMSController.Style(starColumn, urlColumn, serverColumn, versionColumn, lastUpdatedColumn);
    }

    public void setItems() {
        starColumn.setCellValueFactory(new PropertyValueFactory<>("star"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        serverColumn.setCellValueFactory(new PropertyValueFactory<>("server"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("versions"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
    }

    @FXML
    void initialize() {
        setItems();
        filterAll();
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("All");
        list.add("Favorites");
        comboBox.setValue("All");
        comboBox.setItems(list);
        comboBox.setOnAction(e -> {
            String value = comboBox.getValue();
            if(value.equals("Favorites")){
                filterFavorites();
            }else{
                filterAll();
            }
        });
        serverCMSController.linkClick(serverTable);
        style();
    }
}