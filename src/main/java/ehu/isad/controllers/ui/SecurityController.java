package ehu.isad.controllers.ui;


import ehu.isad.controllers.db.SecurityDB;
import ehu.isad.model.SecurityModel;
import ehu.isad.model.ServerCMSModel;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;


public class SecurityController {

    @FXML
    private Pane pane8;

    @FXML
    private TableView<SecurityModel> tableview;

    @FXML
    private TableColumn<SecurityModel, String> targetColumn;

    @FXML
    private TableColumn<SecurityModel, String> IPColumn;

    @FXML
    private TableColumn<SecurityModel, String> countryColumn;

    @FXML
    private Button vuln;

    @FXML
    private MenuItem menuServer;

    @FXML
    private MenuItem menuCMS;

    @FXML
    private MenuItem menuVuln;

    @FXML
    private TextField texfield;

    Desktop desktop = java.awt.Desktop.getDesktop();

    private static SecurityController instance = new SecurityController();

    private SecurityController(){}

    public static SecurityController getInstance() { return instance; }

    public SecurityDB securityDB = SecurityDB.getInstance();

    @FXML
    void onClickVuln(ActionEvent event) {
        SecurityModel sm = tableview.getSelectionModel().getSelectedItem();
        if(sm != null){
            String url = sm.getUrl().getText();
            String text;
            if(url.contains("http:") && securityDB.passwordField(url)){
                text = "This website is vulnerable to Man in The Middle attack and it may also have a form, so it could also be vulnerable to SQLi or XSS attack";
                showMessage("vuln",text);
            }else if(url.contains("http:") && !securityDB.passwordField(url)){
                text = "This website is vulnerable to Man in The Middle attack";
                showMessage("vuln",text);
            }else if(!url.contains("http:") && securityDB.passwordField(url)){
                text = "This website may have a form, so it could be vulnerable to SQLi or XSS attack";
                showMessage("vuln",text);
            }else{
                text = "This website seems to be secure";
                showMessage("notVuln",text);
            }
        }
    }

    private void showMessage(String type, String text){
        Alert alert;
        if(type.equals("vuln")){
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Vulnerabilites found");
        }else{
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("No vulnerabilities found");
        }
        alert.setTitle("Scan completed");
        alert.setContentText(text);
        alert.showAndWait();
    }

    @FXML
    void onClickCMS(ActionEvent event) {
        ServerCMSModel model = getModel(tableview.getSelectionModel().getSelectedItem().getUrl().getText());
        if(model !=null){
            String cms = model.getCms();
            String text;
            if(cms.equals("unknown")){
                text = "This web page doesn't seem to have a cms";
                showCMS_ServerMessages("notvuln",text);
            }else if(!securityDB.isCMSVersionInDB(cms)) {
                text = "This CMS is not supported";
                showCMS_ServerMessages("notvuln", text);
            }else if(model.getVersionc().equals("unknown")){
                text = "This web page's CMS is "+ model.getCms() +". However, the app has not been able to find its version";
                showCMS_ServerMessages("notvuln",text);
            }else{
                if(securityDB.cmsVulnerability(model)){
                    text = "WARNING!!!! This web page's "+ model.getCms() +" CMS version is " + model.getVersionc() + " and the latest version is " + securityDB.getVersion(model.getCms());
                    showCMS_ServerMessages("vuln",text);
                }else{
                    text = "This web page's "+ model.getCms() +" CMS is updated to the latest version";
                    showCMS_ServerMessages("notvuln",text);
                }
            }
        }
    }

    @FXML
    void onClickServer(ActionEvent event) {
        ServerCMSModel model = getModel(tableview.getSelectionModel().getSelectedItem().getUrl().getText());
        if(model !=null){
            String server = model.getServer();
            String text;
            if(server.equals("unknown")){
                text = "This web page doesn't seem to have a server";
                showCMS_ServerMessages("notvuln",text);
            }else if(!securityDB.isCMSVersionInDB(server)){
                text = "This server version is not supported";
                showCMS_ServerMessages("notvuln",text);
            }else if(model.getVersions().equals("unknown")){
                text = "This web page's server is "+ model.getServer() +". However, the app has not been able to find its version";
                showCMS_ServerMessages("notvuln",text);
            }else{
                if(securityDB.serverVulnerability(model)){
                    text = "WARNING!!!! This web page's " + model.getServer() + " server version is " + model.getVersions() + " and the latest version is " + securityDB.getVersion(model.getServer());
                    showCMS_ServerMessages("vuln",text);
                }else{
                    text = "This web page's "+ model.getServer() +" server is updated to the latest version";
                    showCMS_ServerMessages("notvuln",text);
                }
            }
        }
    }

    private void showCMS_ServerMessages(String type, String text) {
        Alert alert;
        if(type.equals("vuln")){
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("WARNING!!!!");
        }else{
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("");
        }
        alert.setTitle("Scan completed");
        alert.setContentText(text);
        alert.showAndWait();
    }

    private ServerCMSModel getModel(String target){
        boolean finish = false;
        ServerCMSModel model = null;
        int i = 0;
        ObservableList<ServerCMSModel> list = ServerCMSController.getInstance().getServerCMSList();
        while(!finish && i < list.size()){
            model = list.get(i);
            if(model.getUrl().getText().equals(target)){
                finish = true;
            }else{
                i++;
            }
        }
        if(!finish) return null;
        else return model;
    }

    void openURL(Hyperlink url) throws IOException {
        if(System.getProperty("os.name").toLowerCase().contains("linux")){
            Runtime.getRuntime().exec("sensible-browser " + url.getText());
        }else{
            desktop.browse(URI.create(url.getText()));
        }
    }

    private void hoverAndLinkClick(){
        tableview.setRowFactory( tr -> {
            final TableRow<SecurityModel> row = new TableRow<>();
            row.setOnMouseMoved(event -> {
                if (! row.isEmpty() ) {
                    Hyperlink hl = row.getItem().getUrl();
                    hl.setOnAction(e -> {
                        try {
                            openURL(hl);
                            hl.setVisited(false);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    });
                }
            });
            return row;
        });
    }

    public void filterAll(){
        FilteredList<SecurityModel> filteredData = new FilteredList<>(securityDB.getFromSecurityDB(), b -> true);
        filter(filteredData);
    }

    private void filter(FilteredList<SecurityModel>filteredData){
        // 2. Set the filter Predicate whenever the filter changes.
        texfield.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(cmsmodel -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Compare first name and last name of every person with filter text.
            String lowerCaseFilter = newValue.toLowerCase();
            if (cmsmodel.getUrl().getText().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Filter matches first name.
            } else // Does not match.
                if (cmsmodel.getUrl().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                } else if(cmsmodel.getIP().toLowerCase().contains(lowerCaseFilter)){
                    return true;
                }   else return cmsmodel.getCountry().toLowerCase().contains(lowerCaseFilter);
        }));
        // 3. Wrap the FilteredList in a SortedList.
        SortedList<SecurityModel> sortedData = new SortedList<>(filteredData);
        // 4. Bind the SortedList comparator to the TableView comparator.
        // 	  Otherwise, sorting the TableView would have no effect.
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());
        // 5. Add sorted (and filtered) data to the table.
        tableview.setItems(sortedData);
    }

    static void Style(TableColumn<SecurityModel, String> targetColumn, TableColumn<SecurityModel, String> IPColumn, TableColumn<SecurityModel, String> countryColumn) {
        targetColumn.setReorderable(false);
        targetColumn.setStyle("-fx-alignment: CENTER;");
        IPColumn.setReorderable(false);
        IPColumn.setReorderable(false);
        countryColumn.setStyle("-fx-alignment: CENTER;");
        countryColumn.setReorderable(false);
    }

    private void style(){ Style(targetColumn, IPColumn, countryColumn); }

    public void setItems() {
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        IPColumn.setCellValueFactory(new PropertyValueFactory<>("IP"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
    }

    @FXML
    void initialize(){
        setItems();
        filterAll();
        style();
        hoverAndLinkClick();
    }

}
