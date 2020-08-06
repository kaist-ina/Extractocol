package extractocol.backend.request.helper;

import extractocol.frontend.MySetupApplication;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.jimple.infoflow.AbstractInfoflow;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.source.parsers.xml.XMLSourceSinkParser;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.rifl.RIFLSourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.options.Options;
import extractocol.frontend.basic.MyCallGraphBuilder;

import javax.activation.UnsupportedDataTypeException;
import java.io.IOException;
import java.util.Collections;
import extractocol.frontend.MyBackwardInfoflow;

/**
 * Created by Administrator on 2017-03-14.
 */
public class JimpleLoader extends AbstractInfoflow {

    public JimpleLoader(String AndroidJar , String ApkPath , String SourceAndSinkPath)
    {
        super(null, AndroidJar, false);
        MySetupApplication msa = new MySetupApplication(AndroidJar, ApkPath, "", null);
        try {
            //We don't need callback finder because we build a callgraph using pre-jimplify option.
            msa.getConfig().setEnableCallbacks(false);
            msa.calculateSourcesSinksEntrypoints(SourceAndSinkPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        //Options.v().setPhaseOption("cg.spark", "pre-jimplify:true");
        String libPath = Scene.v().getAndroidJarPath(AndroidJar, ApkPath);
        
        MyBackwardInfoflow.MySootInitializer(ApkPath, libPath, msa.getEntryPointCreator(), AndroidJar, false);
        
        //initializeSoot(ApkPath, AndroidJar, msa.getEntryPointCreator().getRequiredClasses());
        //ContextInsensitiveBuilder b = new ContextInsensitiveBuilder();
        //if(opts.pre_jimplify()) // Commented by JM
        //b.preJimplify();
    }

    @Override
    public void computeInfoflow(String appPath, String libPath, IEntryPointCreator entryPointCreator, ISourceSinkManager sourcesSinks) {
        return;
    }

    @Override
    public void computeInfoflow(String appPath, String libPath, String entryPoint, ISourceSinkManager sourcesSinks) {
        return;
    }

    @Override
    public InfoflowResults getResults() {
        return null;
    }

    @Override
    public boolean isResultAvailable() {
        return false;
    }
}
