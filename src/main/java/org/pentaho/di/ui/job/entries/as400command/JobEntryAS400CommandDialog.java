package org.pentaho.di.ui.job.entries.as400command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.dialog.AbstractJobEntryDialog;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.as400command.JobEntryAS400Command;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.LabelTextVar;

public class JobEntryAS400CommandDialog extends AbstractJobEntryDialog<JobEntryAS400Command>
		implements JobEntryDialogInterface {

	private static Class<?> PKG = JobEntryAS400Command.class;

	private Button btnTest;

	private LabelTextVar txtServerName;

	private LabelTextVar txtUserName;

	private LabelTextVar txtPassword;

	private LabelTextVar txtProxyHost;

	private LabelTextVar txtProxyPort;

	private LabelTextVar txtCommand;

	public JobEntryAS400CommandDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) {
		super(parent, jobEntry, rep, jobMeta);

		if (Utils.isEmpty(jobEntry.getName())) {
			jobEntry.setName(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Name.Default"));
		}

		setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Shell.Title"));
	}

	public static void main(String[] a) {
		Display display = new Display();
		PropsUI.init(display, Props.TYPE_PROPERTIES_SPOON);
		Shell shell = new Shell(display);

		JobEntryAS400CommandDialog sh = new JobEntryAS400CommandDialog(shell, new JobEntryAS400Command(), null,
				new JobMeta());

		sh.open();
	}

	protected void onTestPressed() {

		String server = txtServerName.getText();
		String user = txtUserName.getText();
		String password = txtPassword.getText();
		String proxyHost = txtProxyHost.getText();
		String proxyPort = txtProxyPort.getText();

		try {
			this.getJobEntry().test(server, user, password, proxyHost, proxyPort);

			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
			mb.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Shell.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Success", server));
			mb.open();

		} catch (Exception e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Shell.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Failed", server,
					e.getMessage()));
			mb.open();
		}

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		super.createButtonsForButtonBar(parent);

		btnTest = new Button(parent, SWT.PUSH);
		btnTest.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Label"));
		btnTest.setLayoutData(new FormDataBuilder().bottom().right(wOK, -ConstUI.SMALL_MARGIN).result());
		btnTest.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				onTestPressed();
			}
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		// The ModifyListener used on all controls. It will update the meta
		// object to
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getJobEntry().setChanged();
			}
		};

		Group systemGroup = new Group(parent, SWT.SHADOW_NONE);
		systemGroup.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.System.Group.Label"));
		FormLayout systemGroupLayout = new FormLayout();
		systemGroupLayout.marginWidth = Const.FORM_MARGIN;
		systemGroupLayout.marginHeight = Const.FORM_MARGIN;
		systemGroup.setLayout(systemGroupLayout);
		systemGroup.setLayoutData(new FormDataBuilder().top().fullWidth().result());
		props.setLook(systemGroup);

		// Widget ServerName
		txtServerName = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Server.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Server.Tooltip"));
		txtServerName.addModifyListener(lsMod);
		txtServerName.setLayoutData(new FormDataBuilder().top().fullWidth().result());
		props.setLook(txtServerName);

		// Widget UserName
		txtUserName = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.User.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.User.Tooltip"));
		txtUserName.setLayoutData(new FormDataBuilder().top(txtServerName).fullWidth().result());
		txtUserName.addModifyListener(lsMod);
		props.setLook(txtUserName);

		// Widget Password
		txtPassword = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Password.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Password.Tooltip"));
		txtPassword.setEchoChar('*');
		txtPassword.setLayoutData(new FormDataBuilder().top(txtUserName).fullWidth().result());
		txtPassword.addModifyListener(lsMod);
		props.setLook(txtPassword);

		Group proxyGroup = new Group(parent, SWT.SHADOW_NONE);
		proxyGroup.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Proxy.Group.Label"));
		FormLayout proxyGroupLayout = new FormLayout();
		proxyGroupLayout.marginWidth = Const.FORM_MARGIN;
		proxyGroupLayout.marginHeight = Const.FORM_MARGIN;
		proxyGroup.setLayout(proxyGroupLayout);
		proxyGroup.setLayoutData(new FormDataBuilder().top(systemGroup, Const.FORM_MARGIN).fullWidth().result());
		props.setLook(proxyGroup);

		// Widget proxy host
		txtProxyHost = new LabelTextVar(jobMeta, proxyGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyHost.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyHost.Tooltip"));
		txtProxyHost.addModifyListener(lsMod);
		txtProxyHost.setLayoutData(new FormDataBuilder().top().fullWidth().result());
		props.setLook(txtProxyHost);

		// Widget UserName
		txtProxyPort = new LabelTextVar(jobMeta, proxyGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyPort.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyPort.Tooltip"));
		txtProxyPort.setLayoutData(new FormDataBuilder().top(txtProxyHost).fullWidth().result());
		txtProxyPort.addModifyListener(lsMod);
		props.setLook(txtProxyPort);

		Group commandGroup = new Group(parent, SWT.SHADOW_NONE);
		commandGroup.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Group.Label"));
		FormLayout commandGroupLayout = new FormLayout();
		commandGroupLayout.marginWidth = Const.FORM_MARGIN;
		commandGroupLayout.marginHeight = Const.FORM_MARGIN;
		commandGroup.setLayout(commandGroupLayout);
		commandGroup.setLayoutData(new FormDataBuilder().top(proxyGroup, Const.FORM_MARGIN).fullWidth().result());
		props.setLook(commandGroup);

		// Widget Command
		txtCommand = new LabelTextVar(jobMeta, commandGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Tooltip"));
		txtCommand.setLayoutData(new FormDataBuilder().fullWidth().result());
		txtCommand.addModifyListener(lsMod);
		props.setLook(txtCommand);

		return parent;
	}

	@Override
	public Point getMinimumSize() {
		return new Point(650, 440);
	}

	@Override
	protected void loadMeta(JobEntryAS400Command jobEntry) {

		txtServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
		txtUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
		txtPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
		txtCommand.setText(Const.NVL(jobEntry.getCommand(), ""));
		txtProxyHost.setText(Const.NVL(jobEntry.getProxyHost(), ""));
		txtProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
	}

	@Override
	protected void saveMeta(JobEntryAS400Command jobEntry) {

		jobEntry.setServerName(txtServerName.getText());
		jobEntry.setUserName(txtUserName.getText());
		jobEntry.setPassword(txtPassword.getText());
		jobEntry.setCommand(txtCommand.getText());
		jobEntry.setProxyHost(txtProxyHost.getText());
		jobEntry.setProxyPort(txtProxyPort.getText());
	}

}
