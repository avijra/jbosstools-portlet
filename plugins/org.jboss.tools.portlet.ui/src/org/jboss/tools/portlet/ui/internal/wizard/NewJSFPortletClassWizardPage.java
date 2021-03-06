package org.jboss.tools.portlet.ui.internal.wizard;

import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CLASS_NAME;
import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.PROJECT;
import static org.eclipse.jst.j2ee.internal.web.operations.INewWebClassDataModelProperties.USE_EXISTING_CLASS;
import static org.eclipse.jst.servlet.ui.internal.wizard.IWebWizardConstants.BROWSE_BUTTON_LABEL;
import static org.eclipse.jst.servlet.ui.internal.wizard.IWebWizardConstants.CLASS_NAME_LABEL;
import static org.jboss.tools.portlet.ui.INewPortletClassDataModelProperties.IS_JSF_PORTLET;
import static org.jboss.tools.portlet.ui.INewPortletClassDataModelProperties.IS_SEAM_PORTLET;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
import org.eclipse.jst.j2ee.internal.wizard.NewJavaClassWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.jboss.tools.portlet.core.IPortletConstants;
import org.jboss.tools.portlet.ui.IPortletUIConstants;
import org.jboss.tools.portlet.ui.Messages;
import org.jboss.tools.portlet.ui.MultiSelectFilteredFileSelectionDialog;

/**
 * JBoss Portlet Wizard Setting Page
 * 
 * @author snjeza
 */
public class NewJSFPortletClassWizardPage extends NewJavaClassWizardPage {
	
	protected Label projectNameLabel;
	private Combo projectNameCombo;
	private String projectName;
	private Label existingClassLabel;
	private Text existingClassText;
	private Button existingClassButton;
	private boolean disableExistingClassButton = false;
	
	public NewJSFPortletClassWizardPage(IDataModel model, String pageName, String pageDesc, String pageTitle,
			String moduleType) {
		super(model, pageName, pageDesc,pageTitle,moduleType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jem.util.ui.wizard.WTPWizardPage#getValidationPropertyNames()
	 */
	protected String[] getValidationPropertyNames() {
		return new String[] { IArtifactEditOperationDataModelProperties.PROJECT_NAME};
	}

	protected Composite createTopLevelComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		composite.setLayoutData(data);
	    Dialog.applyDialogFont(parent);
	    
	    addProjectNameGroup(composite);
	    
	    createUseExistingGroup(composite);
	    
		return composite;
	}
	
	private void createUseExistingGroup(Composite composite) {

		existingClassLabel = new Label(composite, SWT.LEFT);
		existingClassLabel.setText(CLASS_NAME_LABEL);
		existingClassLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));

		existingClassText = new Text(composite, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		existingClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		synchHelper.synchText(existingClassText, CLASS_NAME, null);
		existingClassText.setText(IPortletUIConstants.JBOSS_JSF_PORTLET_CLASS);

		existingClassButton = new Button(composite, SWT.PUSH);
		existingClassButton.setText(BROWSE_BUTTON_LABEL);
		existingClassButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		if (disableExistingClassButton) {
			existingClassButton.setEnabled(false);
		} else {
			existingClassButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleClassButtonSelected();
				}
			});
		}
	}
	
	private void handleClassButtonSelected() {
		getControl().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
		IProject project = (IProject) model.getProperty(PROJECT);
		IVirtualComponent component = ComponentCore.createComponent(project);
		MultiSelectFilteredFileSelectionDialog ms = null;
		if (model.getBooleanProperty(IS_JSF_PORTLET)) {
			ms = new MultiSelectFilteredFileSelectionDialog(
					getShell(), Messages.NewJSFPortletClassWizardPage_New_JSF_Portlet,
					Messages.NewJSFPortletClassWizardPage_Choose_a_JSF_portlet_class, new String[0], false,
					project);
		}
		if (model.getBooleanProperty(IS_SEAM_PORTLET)) {
			ms = new MultiSelectFilteredFileSelectionDialog(
					getShell(), Messages.NewJSFPortletClassWizardPage_New_Seam_Portlet,
					Messages.NewJSFPortletClassWizardPage_Choose_a_Seam_portlet_class, new String[0], false,
					project);
		}
		IContainer root = component.getRootFolder().getUnderlyingFolder();
		ms.setInput(root);
		ms.open();
		if (ms.getReturnCode() == Window.OK) {
			String qualifiedClassName = ""; //$NON-NLS-1$
			IType type = (IType) ms.getFirstResult();
			if (type != null) {
				qualifiedClassName = type.getFullyQualifiedName();
			}
			existingClassText.setText(qualifiedClassName);
		}
		getControl().setCursor(null);
	}
	
	/**
	 * Add project group
	 */
	private void addProjectNameGroup(Composite parent) {
		// set up project name label
		projectNameLabel = new Label(parent, SWT.NONE);
		projectNameLabel.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.MODULES_DEPENDENCY_PAGE_TABLE_PROJECT));
		GridData data = new GridData();
		projectNameLabel.setLayoutData(data);
		// set up project name entry field
		projectNameCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.horizontalSpan = 1;
		projectNameCombo.setLayoutData(data);
		projectNameCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				updateProject();
			}

			public void widgetSelected(SelectionEvent e) {
				updateProject();
			}
			
		});
		
		synchHelper.synchCombo(projectNameCombo, IArtifactEditOperationDataModelProperties.PROJECT_NAME, null);
		initializeProjectList();
		updateProject();
		new Label(parent, SWT.NONE);
	}
	
	protected void updateProject() {
		String projectName = projectNameCombo.getText();
		if (projectName == null || projectName.trim().length() <= 0) {
			return;
		}
		IProject project = ProjectUtilities.getProject(projectName);
		try {
			boolean isSeamPortlet = FacetedProjectFramework.hasProjectFacet(project, IPortletConstants.SEAMPORTLET_FACET_ID);
			boolean isJSFPortlet;
			if (isSeamPortlet) {
				isJSFPortlet = false;
			} else {
				isJSFPortlet = FacetedProjectFramework.hasProjectFacet(project, IPortletConstants.JSFPORTLET_FACET_ID);
			}
			NewJSFPortletWizard wizard = (NewJSFPortletWizard) getWizard(); 
			NewPortletClassDataModelProvider provider = (NewPortletClassDataModelProvider) wizard.getDefaultProvider();
			provider.setSeamPortlet(isSeamPortlet);
			provider.setJSFPortlet(isJSFPortlet);
		} catch (CoreException e) {
			// ignore
		}
	}

	/**
	 * 
	 */
	private void initializeProjectList() {
		IProject[] workspaceProjects = ProjectUtilities.getAllProjects();
		List items = new ArrayList();
		for (int i = 0; i < workspaceProjects.length; i++) {
			IProject project = workspaceProjects[i];
			if (isProjectValid(project))
				items.add(project.getName());
		}
		if (items.isEmpty()) {
			disableExistingClassButton  = true;
			return;
		}
		String[] names = new String[items.size()];
		for (int i = 0; i < items.size(); i++) {
			names[i] = (String) items.get(i);
		}
		projectNameCombo.setItems(names);
		IProject selectedProject = null;
		try {
			if (model !=null) {
				String projectNameFromModel = model.getStringProperty(IArtifactEditOperationDataModelProperties.COMPONENT_NAME);
				if (projectNameFromModel!=null && projectNameFromModel.length()>0)
					selectedProject = ProjectUtilities.getProject(projectNameFromModel);
			}
		} catch (Exception e) {};
		try {
			if (selectedProject == null)
				selectedProject = getSelectedProject();
			if (selectedProject != null && selectedProject.isAccessible()
					&& selectedProject.hasNature(IModuleConstants.MODULE_NATURE_ID)) {
				projectNameCombo.setText(selectedProject.getName());
				model.setProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME, selectedProject.getName());
			}
		} catch (CoreException ce) {
			// Ignore
		}
		if (projectName == null && names.length > 0)
			projectName = names[0];

		if ((projectNameCombo.getText() == null || projectNameCombo.getText().length() == 0) && projectName != null) {
			projectNameCombo.setText(projectName);
			model.setProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME, projectName);
		}

	}
	
	/**
	 * @return
	 */
	private IProject getSelectedProject() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		ISelection selection = window.getSelectionService().getSelection();
		if (selection == null)
			return null;
		if (!(selection instanceof IStructuredSelection)) 
			return null;
		IStructuredSelection stucturedSelection = (IStructuredSelection) selection;
		if (stucturedSelection.getFirstElement() instanceof EObject)
			return ProjectUtilities.getProject(stucturedSelection.getFirstElement());
		IJavaElement element = getInitialJavaElement(selection);
		if (element != null && element.getJavaProject() != null)
			return element.getJavaProject().getProject();
		return getExtendedSelectedProject(stucturedSelection.getFirstElement());
	}
	
	/**
	 * This method is used by the project list initializer. The method checks 
	 * if the specified project is valid to include it in the project list.
	 * 
	 * <p>Subclasses of this wizard page should override this method to 
	 * adjust filtering of the projects to their needs. </p>
	 * 
	 * @param project reference to the project to be checked
	 * 
	 * @return <code>true</code> if the project is valid to be included in 
	 * 		   the project list, <code>false</code> - otherwise. 
	 */
	protected boolean isProjectValid(IProject project) {
		boolean result = super.isProjectValid(project);
		if (!result)
			return result;
		try {
			result = project.isAccessible() && FacetedProjectFramework.hasProjectFacet(project, IPortletConstants.JSFPORTLET_FACET_ID);
			if (!result) {
				return result;
			}
			boolean isSeamProject = model.getBooleanProperty(IS_SEAM_PORTLET);
			if (isSeamProject) {
				result = FacetedProjectFramework.hasProjectFacet(project, IPortletConstants.SEAMPORTLET_FACET_ID);
			}
		} catch (CoreException ce) {
			result = false;
		}
		return result;
	}
	
	public boolean canFlipToNextPage() {
		if (model.getBooleanProperty(USE_EXISTING_CLASS))
			return false;
		return super.canFlipToNextPage();
	}
	
	@Override
	protected boolean showValidationErrorsOnEnter() {
		return true;
	}
	
}
