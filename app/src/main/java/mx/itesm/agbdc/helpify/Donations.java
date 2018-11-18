package mx.itesm.agbdc.helpify;

public class Donations {

    private String Fecha_realizdo, Fecha_solicitud, InsitutionID, InstitutionName, Numero, Status,
            postKey, userID;

    public Donations()
    {

    }

    public Donations(String Fecha_realizdo, String Fecha_solicitud, String InsitutionID, String InstitutionName,
                     String Numero, String Status, String postKey, String userID)
    {
        this.Fecha_realizdo = Fecha_realizdo;
        this.Fecha_solicitud = Fecha_solicitud;
        this.InsitutionID = InsitutionID;
        this.InstitutionName = InstitutionName;
        this.Numero = Numero;
        this.Status = Status;
        this.postKey = postKey;
        this.userID = userID;
    }

    public String getFecha_realizdo() {
        return Fecha_realizdo;
    }

    public void setFecha_realizdo(String fecha_realizdo) {
        Fecha_realizdo = fecha_realizdo;
    }

    public String getFecha_solicitud() {
        return Fecha_solicitud;
    }

    public void setFecha_solicitud(String fecha_solicitud) {
        Fecha_solicitud = fecha_solicitud;
    }

    public String getInsitutionID() {
        return InsitutionID;
    }

    public void setInsitutionID(String insitutionID) {
        InsitutionID = insitutionID;
    }

    public String getInstitutionName() {
        return InstitutionName;
    }

    public void setInstitutionName(String institutionName) {
        InstitutionName = institutionName;
    }

    public String getNumero() {
        return Numero;
    }

    public void setNumero(String numero) {
        Numero = numero;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
