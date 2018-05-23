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

	private Button wTest;

	private LabelTextVar wServerName;

	private LabelTextVar wUserName;

	private LabelTextVar wPassword;

	private LabelTextVar wProxyHost;

	private LabelTextVar wProxyPort;

	private LabelTextVar wCommand;

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

		String server = wServerName.getText();
		String user = wUserName.getText();
		String password = wPassword.getText();
		String proxyHost = wProxyHost.getText();
		String proxyPort = wProxyPort.getText();

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

		wTest = new Button(parent, SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.TestConnection.Label"));
		wTest.setLayoutData(new FormDataBuilder().bottom().right(wOK, -ConstUI.SMALL_MARGIN).result());
		wTest.addListener(SWT.Selection, new Listener() {
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
		wServerName = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Server.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Server.Tooltip"));
		wServerName.addModifyListener(lsMod);
		wServerName.setLayoutData(new FormDataBuilder().top().fullWidth().result());
		props.setLook(wServerName);

		// Widget UserName
		wUserName = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.User.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.User.Tooltip"));
		wUserName.setLayoutData(new FormDataBuilder().top(wServerName).fullWidth().result());
		wUserName.addModifyListener(lsMod);
		props.setLook(wUserName);

		// Widget Password
		wPassword = new LabelTextVar(jobMeta, systemGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Password.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Password.Tooltip"));
		wPassword.setEchoChar('*');
		wPassword.setLayoutData(new FormDataBuilder().top(wUserName).fullWidth().result());
		wPassword.addModifyListener(lsMod);
		props.setLook(wPassword);

		Group proxyGroup = new Group(parent, SWT.SHADOW_NONE);
		proxyGroup.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Proxy.Group.Label"));
		FormLayout proxyGroupLayout = new FormLayout();
		proxyGroupLayout.marginWidth = Const.FORM_MARGIN;
		proxyGroupLayout.marginHeight = Const.FORM_MARGIN;
		proxyGroup.setLayout(proxyGroupLayout);
		proxyGroup.setLayoutData(new FormDataBuilder().top(systemGroup, Const.FORM_MARGIN).fullWidth().result());
		props.setLook(proxyGroup);

		// Widget proxy host
		wProxyHost = new LabelTextVar(jobMeta, proxyGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyHost.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyHost.Tooltip"));
		wProxyHost.addModifyListener(lsMod);
		wProxyHost.setLayoutData(new FormDataBuilder().top().fullWidth().result());
		props.setLook(wProxyHost);

		// Widget UserName
		wProxyPort = new LabelTextVar(jobMeta, proxyGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyPort.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.ProxyPort.Tooltip"));
		wProxyPort.setLayoutData(new FormDataBuilder().top(wProxyHost).fullWidth().result());
		wProxyPort.addModifyListener(lsMod);
		props.setLook(wProxyPort);

		Group commandGroup = new Group(parent, SWT.SHADOW_NONE);
		commandGroup.setText(BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Group.Label"));
		FormLayout commandGroupLayout = new FormLayout();
		commandGroupLayout.marginWidth = Const.FORM_MARGIN;
		commandGroupLayout.marginHeight = Const.FORM_MARGIN;
		commandGroup.setLayout(commandGroupLayout);
		commandGroup.setLayoutData(new FormDataBuilder().top(proxyGroup, Const.FORM_MARGIN).fullWidth().result());
		props.setLook(commandGroup);

		// Widget Command
		wCommand = new LabelTextVar(jobMeta, commandGroup,
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Label"),
				BaseMessages.getString(PKG, "JobEntryAS400CommandDialog.Command.Tooltip"));
		wCommand.setLayoutData(new FormDataBuilder().fullWidth().result());
		wCommand.addModifyListener(lsMod);
		props.setLook(wCommand);

		return parent;
	}

	@Override
	public Point getMinimumSize() {
		return new Point(650, 440);
	}

	@Override
	protected void loadMeta(JobEntryAS400Command jobEntry) {

		wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
		wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
		wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
		wCommand.setText(Const.NVL(jobEntry.getCommand(), ""));
		wProxyHost.setText(Const.NVL(jobEntry.getProxyHost(), ""));
		wProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
	}

	@Override
	protected void saveMeta(JobEntryAS400Command jobEntry) {

		jobEntry.setServerName(wServerName.getText());
		jobEntry.setUserName(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setCommand(wCommand.getText());
		jobEntry.setProxyHost(wProxyHost.getText());
		jobEntry.setProxyPort(wProxyPort.getText());
	}

}
