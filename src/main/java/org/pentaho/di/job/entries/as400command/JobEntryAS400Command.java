package org.pentaho.di.job.entries.as400command;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.ui.job.entries.as400command.JobEntryAS400CommandDialog;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;

/**
 * Calling AS/400 CL Commands
 * 
 * @author Nicolas ADMENT
 *
 */

@JobEntry(
    id = "AS400Command",
    image = "as400command.svg",
    i18nPackageName = "org.pentaho.di.job.entries.as400command",
    name = "JobEntryAS400Command.Name",
    description = "JobEntryAS400Command.Description",
    categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility",
    documentationUrl = "https://github.com/nadment/pdi-as400-plugin/wiki")

public class JobEntryAS400Command extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryAS400Command.class;

  private static final String TAG_SERVER = "server"; //$NON-NLS-1$
 
  private static final String TAG_USER = "user"; //$NON-NLS-1$

  private static final String TAG_PASSWORD = "password"; //$NON-NLS-1$

  private static final String TAG_COMMAND = "command"; //$NON-NLS-1$

  private static final String TAG_PROXY_HOST = "proxyHost"; //$NON-NLS-1$

  private static final String TAG_PROXY_PORT = "proxyPort"; //$NON-NLS-1$

  private String server;

  private String user;

  private String password;

  private String command;

  private String proxyHost;

  private String proxyPort;

  public JobEntryAS400Command() {
    this("", "");
  }

  public JobEntryAS400Command(String name, String description) {
    super(name, description);

    server = null;
    user = null;
    password = null;
    command = null;
  }


  
  @Override
  public Object clone() {
    JobEntryAS400Command cmd = (JobEntryAS400Command) super.clone();
    return cmd;
  }

  /**
   * @return Returns the userName.
   */
  public String getUserName() {
    return user;
  }

  /**
   * Set the username that this job entry  should use for connections.
   * 
   * @param userName
   *            The userName to set.
   */
  public void setUserName(String userName) {
    this.user = userName;
  }

  /**
   * 
   * 
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the password that this job entry  should use for connections.
   * 
   * @param password
   *            The password to set.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return server;
  }

  /**
   * @param serverName
   *            The serverName to set.
   */
  public void setServerName(String serverName) {
    this.server = serverName;
  }

  /**
   * Get the name and port of the proxy server in the format serverName[:port]. If no port is specified, a default will be used.
   * 
   * @return
   */
  protected String getProxyServer(String host, String port) {
    String proxyServer = "";
    if (!Utils.isEmpty(host)) {
      proxyServer = environmentSubstitute(port);
      if (!Utils.isEmpty(port))
        proxyServer = proxyServer + ":" + environmentSubstitute(port);
    }

    return proxyServer;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String value) {
    this.proxyHost = value;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(String value) {
    this.proxyPort = value;
  }

  @Override
  public Result execute(final Result result, int nr) throws KettleException {

    AS400 system = null;

    if (isBasic()) {
      logBasic(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.Started"));
    }

    // Resolve variables
    String serverString = environmentSubstitute(server);
    String userString = environmentSubstitute(user);
    String passwordString = Utils.resolvePassword(this, password);
    String commandString = environmentSubstitute(command);

    try {
      // Create proxy server    
      String proxyServer = this.getProxyServer(environmentSubstitute(proxyHost), environmentSubstitute(proxyPort));

      // Create an AS400 object      
      if (isBasic()) {
        logBasic(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.Connecting", serverString,
            environmentSubstitute(user)));
      }
      system = new AS400(serverString, userString, passwordString, proxyServer);

      // Connect to service
      if (isBasic()) {
        logBasic(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.Connected", serverString));
      }
      system.connectService(AS400.COMMAND);

      // Run the command
      if (isBasic()) {
        logBasic(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.CommandRun", commandString));
      }
      final CommandCall commandCall = new CommandCall(system);
      boolean success = commandCall.run(commandString);

      if (success) {
        if (isBasic()) {

          logBasic(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.CommandSuccess", serverString, commandString));
        }
        result.setNrErrors(0);
        result.setResult(true);
      } else {
        logError(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.CommandFailure", serverString, commandString));

        
        // Get the command results
        AS400Message[] messageList = commandCall.getMessageList();
        for (AS400Message message : messageList) {
          logError(message.getText());
          logError(message.getHelp());
        }
        result.setNrErrors(1);
        result.setResult(false);
      }
    } catch (Exception e) {
      logError(BaseMessages.getString(PKG, "JobEntryAS400Command.Log.CommandFailure", serverString, commandString), e);
    } finally {
      try {
        // Make sure to disconnect
        system.disconnectService(AS400.COMMAND);
      } catch (Exception e) {
        // Ignore
      }
    }

    return result;
  }

  /**
   * The command to run on the AS/400
   * 
   * @return
   */
  public String getCommand() {
    return command;
  }

  public void setCommand(final String command) {
    this.command = command;
  }

  @Override
  public String getXML() {
    StringBuilder xml = new StringBuilder(100);

    xml.append(super.getXML());
    xml.append(XMLHandler.addTagValue(TAG_SERVER, server));
    xml.append(XMLHandler.addTagValue(TAG_USER, user));
    xml.append(XMLHandler.addTagValue(TAG_PASSWORD, Encr.encryptPasswordIfNotUsingVariables(password)));
    xml.append(XMLHandler.addTagValue(TAG_PROXY_HOST, proxyHost));
    xml.append(XMLHandler.addTagValue(TAG_PROXY_PORT, proxyPort));
    xml.append(XMLHandler.addTagValue(TAG_COMMAND, command));

    return xml.toString();
  }

  @Override
  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);

      server = XMLHandler.getTagValue(entrynode, TAG_SERVER);
      user = XMLHandler.getTagValue(entrynode, TAG_USER);
      password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, TAG_PASSWORD));

      proxyHost = XMLHandler.getTagValue(entrynode, TAG_PROXY_HOST);
      proxyPort = XMLHandler.getTagValue(entrynode, TAG_PROXY_PORT);

      command = XMLHandler.getTagValue(entrynode, TAG_COMMAND);

    } catch (KettleXMLException xe) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryAS400Command.Exception.UnableToReadXML"), xe);
    }
  }

  @Override
  public void loadRep(Repository repository, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers) throws KettleException {
    try {
      server = repository.getJobEntryAttributeString(id_jobentry, TAG_SERVER);
      user = repository.getJobEntryAttributeString(id_jobentry, TAG_USER);
      password = Encr
          .decryptPasswordOptionallyEncrypted(repository.getJobEntryAttributeString(id_jobentry, TAG_PASSWORD));
      proxyHost = repository.getJobEntryAttributeString(id_jobentry, TAG_PROXY_HOST);
      proxyPort = repository.getJobEntryAttributeString(id_jobentry, TAG_PROXY_PORT);

      command = repository.getJobEntryAttributeString(id_jobentry, TAG_COMMAND);
    } catch (KettleException dbe) {
      throw new KettleException(
          BaseMessages.getString(PKG, "JobEntryAS400Command.Exception.UnableToReadRepository", id_jobentry), dbe);

    }
  }

  @Override
  public void saveRep(Repository repository, IMetaStore metaStore, ObjectId id_job) throws KettleException {
    try {
      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_SERVER, server);
      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_USER, user);
      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_PASSWORD,
          Encr.encryptPasswordIfNotUsingVariables(password));

      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_PROXY_HOST, proxyHost);
      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_PROXY_PORT, proxyPort);

      repository.saveJobEntryAttribute(id_job, getObjectId(), TAG_COMMAND, command);
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          BaseMessages.getString(PKG, "JobEntryAS400Command.Exception.UnableToSaveRepository", id_job), dbe);
    }
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return true;
  }

  @Override
  public String getDialogClassName() {
    return JobEntryAS400CommandDialog.class.getCanonicalName();
  }

  @Override
  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Utils.isEmpty(server)) {
      String realServername = jobMeta.environmentSubstitute(server);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add(new ResourceEntry(realServername, ResourceType.SERVER));
      references.add(reference);
    }
    return references;
  }

  public boolean test(final String server, final String user, final String password, final String proxyHost,
      final String proxyPort) throws Exception {

    // Create proxy server    
    String proxyServer = this.getProxyServer(environmentSubstitute(proxyHost), environmentSubstitute(proxyPort));

    // Create an AS400 object
    AS400 system = new AS400(environmentSubstitute(server), environmentSubstitute(user),
        Utils.resolvePassword(this, password), proxyServer);
    system.connectService(AS400.COMMAND);

    return true;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository,
      IMetaStore metaStore) {
	  JobEntryValidatorUtils.andValidator().validate(this, TAG_SERVER, remarks,  AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator()));
	  JobEntryValidatorUtils.andValidator().validate(this, TAG_USER, remarks,  AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator()));
	  JobEntryValidatorUtils.andValidator().validate(this, TAG_PASSWORD, remarks,  AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator()));
	  JobEntryValidatorUtils.andValidator().validate(this, TAG_COMMAND, remarks,  AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator()));
  }

}
