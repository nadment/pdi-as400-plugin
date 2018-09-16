package org.pentaho.di.dialog;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

public abstract class AbstractJobEntryDialog<T extends JobEntryInterface> extends JobEntryDialog
    implements JobEntryDialogInterface {

  public static final int LARGE_MARGIN = 15;

  private final JobEntryInterface jobEntry;

  private boolean changed;

  protected Button wCancel;

  protected Button wOK;

  private Text wJobEntryName;

  //	/**
  //	 * Listeners for the common dialog buttons.
  //	 */
  //protected Listener lsMod;

  protected static final int BUTTON_WIDTH = 80;

  public AbstractJobEntryDialog(final Shell parent, final JobEntryInterface jobEntryInt, final Repository rep,
      final JobMeta jobMeta) {
    super(parent, jobEntryInt, rep, jobMeta);

    this.jobEntry = jobEntryInt;

    //setText(BaseMessages.getString(PKG, jobEntry.getClass().getAnnotation(org.pentaho.di.core.annotations.JobEntry.class).name()));
  }

  
  @SuppressWarnings("unchecked")
  public T getJobEntry() {
    return (T) jobEntry;
  }

  public Image getImage() {

    PluginInterface plugin = PluginRegistry.getInstance().getPlugin(JobEntryPluginType.class, jobEntry);

    if (plugin.getImageFile() != null) {
      return SwtSvgImageUtil.getImage(shell.getDisplay(), getClass().getClassLoader(), plugin.getImageFile(),
          ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
    }

    return GUIResource.getInstance().getImageStepError();
  }

  /**
   * Returns a point describing the minimum receiver's size. The x coordinate of the result is the minimum width of the receiver. The y coordinate of the
   * result is the minimum height of the receiver.
   * 
   * @return the receiver's size 
   */
  public Point getMinimumSize() {
    return new Point(100, 50);
  }

  protected boolean isValid() {
    return !Utils.isEmpty(this.wJobEntryName.getText());
  }

  @Override
  public JobEntryInterface open() {

    Shell parent = getParent();
    Display display = parent.getDisplay();

    // Create shell
    // shell = new Shell(parent, props.getJobsDialogStyle());
    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE); //| SWT.MAX | SWT.MIN);
    shell.setText(getText());

    JobDialog.setShellImage(shell, jobEntry);
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = LARGE_MARGIN;
    formLayout.marginHeight = LARGE_MARGIN;
    shell.setLayout(formLayout);
    shell.setMinimumSize(getMinimumSize());
    props.setLook(shell);

    this.createContents(shell);

    // Populate the dialog with the values from the meta object
    changed = jobEntry.hasChanged();
    loadMeta(this.getJobEntry());
    jobEntry.setChanged(changed);

    // Set focus on job entry name
    wJobEntryName.setText(jobEntry.getName());
    wJobEntryName.selectAll();
    wJobEntryName.setFocus();

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        onCancelPressed();
      }
    });

    // Set/Restore the dialog size based on last position on screen
    setSize(shell);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    return jobEntry;
  }

  /**
   * Set the shell size, based upon the previous time the geometry was saved in the Properties file.
  *
    * @param shell     the shell
    */
  public void setSize(final Shell shell) {
    PropsUI props = PropsUI.getInstance();

    WindowProperty winprop = props.getScreen(shell.getText());
    if (winprop != null) {
      winprop.setShell(shell, true);
    } else {

      shell.pack();

      
      // OK, sometimes this produces dialogs that are waay too big.
      // Try to limit this a bit, m'kay?
      // Use the same algorithm by cheating :-)
      //

      Point minSize = shell.getMinimumSize();

      winprop = new WindowProperty(shell);
      winprop.setShell(shell, minSize.x, minSize.y);

      // Now, as this is the first time it gets opened, try to put it in the middle of the screen...
      Rectangle shellBounds = shell.getBounds();
      Monitor monitor = shell.getDisplay().getPrimaryMonitor();
      if (shell.getParent() != null) {
        monitor = shell.getParent().getMonitor();
      }
      Rectangle monitorClientArea = monitor.getClientArea();

      int middleX = monitorClientArea.x + (monitorClientArea.width - shellBounds.width) / 2;
      int middleY = monitorClientArea.y + (monitorClientArea.height - shellBounds.height) / 2;

      shell.setLocation(middleX, middleY);
    }
  }

  /**
   * Creates and returns the contents of the upper part of this dialog (above the button bar).
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates and returns a new <code>Composite</code> with no margins and spacing. Subclasses
   * should override.
   * </p>
   * 
   * @param parent
   *            The parent composite to contain the dialog area
   * @return the dialog area control
   */
  protected abstract Control createDialogArea(final Composite parent);

  protected final Control createContents(final Composite parent) {

    Control titleArea = this.createTitleArea(parent);

    // The title separator line
    Label titleSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
    titleSeparator.setLayoutData(new FormDataBuilder().top(titleArea, LARGE_MARGIN).fullWidth().result());
    props.setLook(titleSeparator);

    // The button bar
    Control buttonBar = this.createButtonBar(parent);
    buttonBar.setLayoutData(new FormDataBuilder().fullWidth().bottom().result());

    // The bottom separator line
    Label bottomSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
    bottomSeparator.setLayoutData(new FormDataBuilder().bottom(buttonBar, -LARGE_MARGIN).fullWidth().result());
    props.setLook(bottomSeparator);

    Composite area = new Composite(parent, SWT.NONE);
    area.setLayout(new FormLayout());
    area.setLayoutData(new FormDataBuilder().top(titleSeparator, LARGE_MARGIN).bottom(bottomSeparator, -LARGE_MARGIN)
        .fullWidth().result());
    props.setLook(area);

    this.createDialogArea(area);

    return area;
  }

  protected final Control createTitleArea(final Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FormLayout());
    composite.setLayoutData(new FormDataBuilder().top().fullWidth().result());
    props.setLook(composite);

    Label icon = new Label(composite, SWT.CENTER);
    icon.setImage(getImage());
    icon.setLayoutData(new FormDataBuilder().top().right(100, 0).width(ConstUI.ICON_SIZE).result());
    props.setLook(icon);

    Label label = new Label(composite, SWT.NONE);
    //label.setText(BaseMessages.getString("System.Label.StepName"));
    label.setText("Job entry name");
    label.setLayoutData(new FormDataBuilder().top().left().right(icon, 100).result());
    props.setLook(label);

    // Widget Job entry name
    wJobEntryName = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wJobEntryName.setLayoutData(new FormDataBuilder().top(label).left().right(icon, -ConstUI.ICON_SIZE).result());
    // wJobEntryName.addModifyListener(lsMod);
    // wJobEntryName.addSelectionListener(lsDef);
    props.setLook(wJobEntryName);

    final ControlDecoration deco = new ControlDecoration(wJobEntryName, SWT.TOP | SWT.LEFT);
    deco.setDescriptionText(BaseMessages.getString("System.JobEntryNameMissing.Msg"));
    deco.setImage(
        FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
    deco.setShowOnlyOnFocus(true);
    deco.hide();

    wJobEntryName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (wJobEntryName.getText().length() > 0) {
          deco.hide();
        } else {
          deco.show();
        }

        jobEntry.setChanged();

        wOK.setEnabled(isValid());
      }
    });

    return composite;
  }

  protected Control createButtonBar(final Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FormLayout());
    composite.setLayoutData(new FormDataBuilder().fullWidth().bottom().result());
    composite.setFont(parent.getFont());
    props.setLook(composite);

    //		// Build the separator line
    //		Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
    //		titleBarSeparator.setLayoutData(new FormDataBuilder().top().fullWidth().result());

    // Add the buttons to the button bar.
    this.createButtonsForButtonBar(composite);

    return composite;
  }

  protected void createButtonsForButtonBar(final Composite parent) {
    wCancel = new Button(parent, SWT.PUSH);
    wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
    wCancel.setLayoutData(new FormDataBuilder().bottom().right().width(BUTTON_WIDTH).result());
    wCancel.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onCancelPressed();
      }
    });

    wOK = new Button(parent, SWT.PUSH);
    wOK.setText(BaseMessages.getString("System.Button.OK"));
    wOK.setLayoutData(
        new FormDataBuilder().bottom().right(wCancel, -ConstUI.SMALL_MARGIN).width(BUTTON_WIDTH).result());
    wOK.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onOkPressed();
      }
    });
  }

  /**
  * This helper method takes the job entry configuration stored in the meta object and puts it into the dialog controls.
  */
  protected abstract void loadMeta(T jobEntry);

  /**
   * This helper method takes the information configured in the dialog controls and stores it into the job entry configuration meta object
   */
  protected abstract void saveMeta(T jobEntry);

  /**
   * Dispose this dialog.
   */
  public void dispose() {
    WindowProperty winprop = new WindowProperty(shell);
    props.setScreen(winprop);
    shell.dispose();
  }

  /**
   * Called when the user confirms the dialog. Subclasses may override if desired.
   */
  protected void onOkPressed() {

    if (Utils.isEmpty(wJobEntryName.getText())) {      
      return;
    }
    
    jobEntry.setName(wJobEntryName.getText());

    saveMeta(this.getJobEntry());

    // Close the SWT dialog window
    dispose();
  }

  /**
   * Called when the user cancels the dialog. Subclasses may override if desired.
   */
  protected void onCancelPressed() {

    // Restore initial state
    jobEntry.setChanged(changed);

    // Close the SWT dialog window
    dispose();
  }

}
