package py.edu.cursojava.controladores;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;
import py.edu.cursojava.dao.ClienteDao;
import py.edu.cursojava.entidades.Cliente;
import py.edu.cursojava.interfaces.InterfaceAcciones;
import py.edu.cursojava.tablas.ModeloTablaCliente;
import py.edu.cursojava.utilidades.EventosGenericos;
import py.edu.cursojava.utilidades.UtilidadesFecha;
import py.edu.cursojava.vistas.ClienteVentana;

public class ClienteController implements InterfaceAcciones {
	
	private ClienteVentana ventana;
	private ModeloTablaCliente modeloTablaCliente;
	private Cliente cliente;
	private List<Cliente> clientes;
	private ClienteDao dao;
	

	public ClienteController(ClienteVentana clienteVentana) {
		super();
		this.ventana= clienteVentana;
		this.ventana.setInterfaceAcciones(this);
		modeloTablaCliente = new ModeloTablaCliente();
		this.ventana.getTable().setModel(modeloTablaCliente);
		dao = new ClienteDao();
		consultarClientes();
		estadoinicial();
		setUpEvents();
	}
	
	private void setUpEvents() {
		ventana.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) seleccionarRegistro(ventana.getTable().getSelectedRow());
			}
		});
		ventana.getTfBuscador().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar()==KeyEvent.VK_ENTER) consultarClientes();;
			}
		});
		
	}
	
	private void estadoinicial() {
		ventana.getBtnCancelar().setEnabled(false);
		ventana.getBtnModificar().setEnabled(false);
		ventana.getBtnEliminar().setEnabled(false);
		ventana.getBtnGuardar().setEnabled(false);
		ventana.getBtnNuevo().setEnabled(true);
		EventosGenericos.estadosJtexField(ventana.getjPanelFormulario(), false);
		EventosGenericos.limpiarJtexField(ventana.getjPanelFormulario());
		ventana.getTfFecha().setValue(null);;
		ventana.getChbEstado().setSelected(true);;
		ventana.getTfFecha().setEnabled(false);
		ventana.getChbEstado().setEnabled(false);
		ventana.getBtnGuardar().setText("Guardar");
		ventana.getTable().setEnabled(true);
	}

	@Override
	public void nuevo() {
		ventana.getBtnNuevo().setEnabled(false);
		ventana.getBtnSalir().setEnabled(false);
		ventana.getBtnGuardar().setEnabled(true);
		ventana.getBtnCancelar().setEnabled(true);
		EventosGenericos.estadosJtexField(ventana.getjPanelFormulario(), true);
		ventana.getTfFecha().setEnabled(true);
		ventana.getChbEstado().setEnabled(true);
		ventana.getTable().setEnabled(false);
	}

	@Override
	public void modificar() {
		EventosGenericos.estadosJtexField(ventana.getjPanelFormulario(), true);
		ventana.getTfFecha().setEnabled(true);
		ventana.getChbEstado().setEnabled(true);
		ventana.getBtnCancelar().setEnabled(true);
		ventana.getBtnGuardar().setEnabled(true);
		ventana.getBtnGuardar().setText("Actualizar");
	}

	@Override
	public void eliminar() {
		if (cliente==null) {
			JOptionPane.showMessageDialog(null, "Cliente no encontrado");
			return;
		}
		int respuesta = JOptionPane.showConfirmDialog(null, "Estas deguro que desea eliminar el cliente "+cliente.getNombre(),
				"Antenci�n", JOptionPane.YES_NO_OPTION);
		if (respuesta==JOptionPane.YES_OPTION) {
			try {
				dao.eliminar(cliente);
				dao.commit();
				estadoinicial();
				consultarClientes();
			} catch (Exception e) {
				dao.rollback();
				e.printStackTrace();
			}
		}
	}

	@Override
	public void salir() {
		ventana.dispose();
	}

	@Override
	public void cancelar() {
		estadoinicial();
	}

	@Override
	public void guardar() {
		if (!validarCampos()) return;
		cliente = new Cliente();
		cargarEntidad();
		try {
			dao.guardar(cliente);
			dao.commit();
			consultarClientes();
			estadoinicial();
		} catch (Exception e) {
			dao.rollback();
			e.printStackTrace();
		}
	}

	@Override
	public void actualizar() {
		if (!validarCampos()) return;
		cargarEntidad();
		try {
			dao.guardar(cliente);
			dao.commit();
			consultarClientes();
			estadoinicial();
		} catch (Exception e) {
			dao.rollback();
			e.printStackTrace();
		}
	}
	
	private void cargarEntidad() {
		cliente.setNombre(this.ventana.getTfNombre().getText());
		cliente.setApellido(this.ventana.getTfApellido().getText());
		cliente.setDocumento(this.ventana.getTfDocumento().getText());
		cliente.setTelefono(this.ventana.getTfTelefono().getText());
		cliente.setEmail(this.ventana.getTfEmail().getText());
		cliente.setDireccion(this.ventana.getTfDireccion().getText());
		
		cliente.setFechaRegistro(UtilidadesFecha.stringAFecha(this.ventana.getTfFecha().getText()));
		cliente.setEstado(this.ventana.getChbEstado().isSelected());
	}
	
	private void seleccionarRegistro(int index) {
		if(index<0)return;
		cliente = clientes.get(index);
		ventana.getTfNombre().setText(cliente.getNombre());
		ventana.getTfApellido().setText(cliente.getApellido());
		ventana.getTfDocumento().setText(cliente.getDocumento());
		ventana.getTfTelefono().setText(cliente.getTelefono());
		ventana.getTfDireccion().setText(cliente.getDireccion());
		ventana.getTfEmail().setText(cliente.getEmail());
		ventana.getTfFecha().setText(UtilidadesFecha.fechaAString(cliente.getFechaRegistro()));
		ventana.getChbEstado().setSelected(cliente.isEstado());
		
		ventana.getBtnNuevo().setEnabled(false);
		ventana.getBtnSalir().setEnabled(false);
		ventana.getBtnModificar().setEnabled(true);
		ventana.getBtnEliminar().setEnabled(true);
		
	}
	
	private void consultarClientes() {
		clientes = dao.filtrarClientes(this.ventana.getTfBuscador().getText());
		modeloTablaCliente.setLista(clientes);
	}
	
	private boolean validarCampos() {
		if (ventana.getTfNombre().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Nombre es obligatorio");
			return false;
		}
		if (ventana.getTfApellido().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Apellido es obligatorio");
			return false;
		}
		if (ventana.getTfDocumento().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Documento es obligatorio");
			return false;
		}
		if (ventana.getTfTelefono().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Telefono es obligatorio");
			return false;
		}
		if (ventana.getTfEmail().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Email es obligatorio");
			return false;
		}
		if (ventana.getTfDireccion().getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "El campo Direcci�n es obligatorio");
			return false;
		}
		if (UtilidadesFecha.stringAFecha(ventana.getTfFecha().getText())==null) {
			JOptionPane.showMessageDialog(null, "Ingrese una fecha valida");
			return false;
		}
		if (dao.verificarCedula(ventana.getTfDocumento().getText())!=null) {
			if(cliente!=null & cliente.getId() == dao.verificarCedula(ventana.getTfDocumento().getText()).getId()) return true;
			JOptionPane.showMessageDialog(null, "Documento Duplicado");
			return false;
		}
		
		return true;
	}
	
	

}
