import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Clinica {

	private Connection conn;

	private void showMenu() throws Exception {
		conectar();
		int option = -1;
		do {
			System.out
					.println("-------------------------------------------------------");
			System.out.println("Bienvenido a CLINICA\n");
			System.out.println("Selecciona una opción:\n");
			System.out
					.println("\t1. Obtener la lista completa de doctores que hay en la base de datos.");
			System.out
					.println("\t2. Obtener la lista de pacientes disponibles.");
			System.out
					.println("\t3. Obtener la lista de enfermedades que hay en la base de datos.");
			System.out
					.println("\t4. Obtener la lista de fármacos disponibles.");
			System.out.println("\t5. Obtener los pacientes de un doctor dado.");
			System.out
					.println("\t6. Obtener el diagnóstico de un paciente determinado.");
			System.out
					.println("\t7. Obtener cuantas veces una enfermedad ha sido diagnosticada (y la distribución de pacientes que han sufrido dicha enfermedad y cuantas veces).");
			System.out
					.println("\t8. Obtener la lista de enfermedades que pueden ser tratadas con un fármaco concreto.");
			System.out
					.println("\t9. Añadir un nuevo paciente y asociarle una o varias enfermedades.");
			System.out.println("\t10. Salir.");
			try {
				option = readInt();
				System.out.println("----------------------CONSULTA_" + option
						+ "-----------------------");
				switch (option) {
				case 1:
					obtenerDoctores();
					break;
				case 2:
					obtenerPacientes();
					break;
				case 3:
					obtenerEnfermedades();
					break;
				case 4:
					obtenerFarmacos();
					break;
				case 5:
					obtenerPacientesDeUnDoctor();
					break;
				case 6:
					obtenerDiagnosticoDeUnPaciente();
					break;
				case 7:
					obtenerDistribucionDiagnosticoEnfermedad();
					break;
				case 8:
					obtenerEnfermedadesTratadasConFarmaco();
					break;
				case 9:
					anadirPacienteYDiagnosticos();
					break;
				}
			} catch (Exception e) {
				System.err.println("Opción introducida no válida!");
			}
		} while (option != 10);
		exit();
	}

	private void exit() throws SQLException {
		System.out.println("Saliendo.. ¡hasta otra!");
		conn.close();
		System.exit(0);
	}

	private void conectar() throws Exception {
		String drv = "com.mysql.jdbc.Driver";
		Class.forName(drv);
		String serverAddress = "localhost:3306";
		String db = "clinica";
		String user = "clinica";
		String pass = "clicli_pwd";
		String url = "jdbc:mysql://" + serverAddress + "/" + db;
		conn = DriverManager.getConnection(url, user, pass);
		System.out.println("Conectado a la base de datos!");
	}

	private void anadirPacienteYDiagnosticos() {
		// Insert new column 'foto'
		try {

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM paciente");
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rsmd.getColumnCount() == 3)
				st.executeUpdate("ALTER TABLE paciente ADD COLUMN foto LONGBLOB NULL AFTER id_doctor");
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Count(id) pacientes
		int nPacientes = 0;
		try {

			Statement st = conn.createStatement();
			PreparedStatement pst_1 = null;
			PreparedStatement pst_2 = null;
			ResultSet rs = st
					.executeQuery("SELECT count(id) FROM clinica.paciente");
			if (rs.next())
				nPacientes = rs.getInt(1);
			rs.close();
			st.close();

			// Set autocommit to false
			try {
				if (conn.getAutoCommit())
					conn.setAutoCommit(false);
			} catch (SQLException esql) {
				System.err.println("Something went wrong on AutoCommit!");
				printState(esql);
			}
			// Transaction
			try {
				String query = "INSERT INTO paciente (id, nombre, id_doctor,foto) VALUES (?,?,?,?)";
				pst_1 = conn.prepareStatement(query);

				String nombre = null;
				int idDoctor = 0;
				String ruta = null;
				// Read data
				try {
					System.out.println("Introduzca el nombre del paciente:");
					nombre = readString();
					obtenerDoctores();
					System.out.println("Introduzca el id de su doctor:");
					idDoctor = readInt();
					System.out.println("Introduzca la ruta de la foto:");
					ruta = readString();
				} catch (Exception e) {
					System.err.println("Something went wrong with the input!");
				}
				// Prepare the statement
				try {
					pst_1.setInt(1, nPacientes + 1);
					pst_1.setString(2, nombre);
					pst_1.setInt(3, idDoctor);

					try {
						File file = new File(ruta);
						FileInputStream fis = new FileInputStream(file);
						pst_1.setBinaryStream(4, fis, (int) file.length());
					} catch (SQLException esql) {
						printState(esql);
					} catch (Exception e) {
						System.err.println("The photo was not found!");
					}
					pst_1.executeUpdate();
					System.out.println("¡Datos procesados correctamente!");
					int option = 0;
					while (option != 2) {
						System.out.println("Selecciona una opción:\n");
						System.out.println("\t1. Añadir un diagnóstico.");
						System.out.println("\t2. Terminar (Guardar).");

						option = readInt();
						switch (option) {
						// Add diagnosis
						case 1:
							String query_2 = "INSERT INTO diagnostica (fecha, id_paciente, id_enfermedad) VALUES (?,?,?)";
							 pst_2 = conn
									.prepareStatement(query_2);

							String fecha = null;
							int id_enfermedad = 0;
							// Read data
							try {
								System.out
										.println("Introduzca la fecha del diagnóstico (YY-MM-DD):");
								fecha = readString();
								obtenerEnfermedades();
								System.out
										.println("Introduzca el id de la enfermedad:");
								id_enfermedad = readInt();
							} catch (Exception e) {
								System.err
										.println("Something went wrong with the input!");
							}
							// Prepare the statement
							try {
								pst_2.setString(1, fecha);
								pst_2.setInt(2, nPacientes + 1);
								pst_2.setInt(3, id_enfermedad);
								pst_2.executeUpdate();
								System.out
										.println("¡Datos procesados correctamente!");
							} catch (SQLException esql) {
								printState(esql);
							}
							break;
						// Reserved
						case 2:
							// Exit while
							break;
						// TODO: add more options
						}
					}
					//Close resources
					pst_1.close();
					pst_2.close();
					st.close();
					// Statement 1 catch
				} catch (SQLException esql) {
					printState(esql);
				}

				// Everything went fine if reached this point
				conn.commit();

			} catch (SQLException esql) {
				System.err
						.println("Something went wrong adding a new patient!");
				printState(esql);

			} catch (Exception e) {
				System.err.println("Something went wrong somewhere!");
			}

			// Catch Count(id) pacientes
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Set autocommit to true
			try {
				if (!conn.getAutoCommit())
					conn.setAutoCommit(true);
			} catch (SQLException esql) {
				System.err.println("Something went wrong on AutoCommit!");
				printState(esql);
			}
		
		}

	}

	private void obtenerEnfermedadesTratadasConFarmaco() throws Exception {
		obtenerFarmacos();
		System.out.println("Introduzca el id del fármaco:" + '\n');
		int idFarmaco = readInt();
		System.out.println("El fármaco trata las siguientes enfermedades:");
		String format = "|%1$-5s|%2$-15s|\n";
		System.out.format(format, "ID", "Enfermedad");
		System.out.format(format, "--", "----------");
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT id_enfermedad,nombre  FROM clinica.trata "
						+ "INNER JOIN enfermedad "
						+ "ON enfermedad.id = id_enfermedad "
						+ "where id_medicamento = " + idFarmaco);
		while (rs.next())
			System.out.format(format, rs.getString("id_enfermedad"),
					rs.getString("nombre"));
		rs.close();
		st.close();
	}

	private void obtenerDistribucionDiagnosticoEnfermedad() throws Exception {
		obtenerEnfermedades();
		System.out
				.println("Introduzca el id de la enfermad a consultar:" + '\n');
		int idEnfermedad = readInt();
		String format = "|%1$-22s|\n";
		System.out.format(format, "Número de diagnósticos");
		System.out.format(format, "----------------------");
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT COUNT(id_enfermedad) AS nVeces "
				+ "FROM diagnostica " + "WHERE id_enfermedad =" + idEnfermedad);
		while (rs.next())
			System.out.format(format, rs.getString("nVeces"));
		System.out.println("+----------------------+\n\n");
		rs.close();
		st.close();
		String format_2 = "|%1$-10s|%2$-10s|%3$-10s|%4$-20s|%5$-25s|\n";
		System.out.format(format_2, "ID", "Nombre", "ID Doctor",
				"Nombre Doctor", "Diagnósticos por paciente");
		System.out.format(format_2, "--", "------", "--------",
				"--------------", "-------------------------");
		Statement st_2 = conn.createStatement();
		ResultSet rs_2 = st_2
				.executeQuery("SELECT paciente.id as id, paciente.nombre as nombre, doctor.nombre as nombre_doctor, doctor.id as id_doctor, COUNT(id_enfermedad) as sumEnf "
						+ "FROM paciente "
						+ "INNER JOIN doctor"
						+ " ON paciente.id_doctor = doctor.id "
						+ "INNER JOIN diagnostica"
						+ " ON paciente.id = diagnostica.id_paciente where diagnostica.id_enfermedad = "
						+ idEnfermedad + " GROUP BY paciente.id");
		while (rs_2.next()) {
			System.out.format(format_2, rs_2.getInt("id"),
					rs_2.getString("nombre"), rs_2.getInt("id_doctor"),
					rs_2.getString("nombre_doctor"), rs_2.getInt("sumEnf"));
		}
		rs_2.close();
		st_2.close();

	}

	private void obtenerDiagnosticoDeUnPaciente() throws Exception {
		obtenerPacientes();
		System.out
				.println("Por favor introduzca el id perteneciente al paciente de la consulta.");
		int idPaciente = readInt();
		System.out.println("Los diagnosticos del paciente con id " + idPaciente
				+ " son:");
		String format = "|%1$-20s|%2$-15s|\n";
		System.out.format(format, "Diagnostico", "Fecha");
		System.out.format(format, "-----------", "-----");
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT diagnostica.fecha, enfermedad.nombre as diagnostico"
						+ " FROM diagnostica "
						+ "INNER JOIN enfermedad ON diagnostica.id_enfermedad = enfermedad.id where id_paciente ="
						+ idPaciente);
		while (rs.next()) {
			System.out.format(format, rs.getString("diagnostico"),
					rs.getString("fecha"));
		}
		rs.close();
		st.close();
	}

	private void obtenerPacientesDeUnDoctor() throws Exception {
		obtenerDoctores();
		System.out
				.println("Por favor introduzca el id perteneciente al doctor de la consulta.");
		int idDoctor = readInt();
		System.out.println("Los pacientes del doctor con id " + idDoctor
				+ " son:");
		String format = "|%1$-10s|%2$-10s|\n";
		System.out.format(format, "ID", "Nombre");
		System.out.format(format, "--", "------");
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT * "
						+ "FROM `clinica`.`paciente` "
						+ "where id_doctor = (SELECT id FROM `clinica`.`doctor` where  id= "
						+ idDoctor + ")");
		while (rs.next()) {
			System.out.format(format, rs.getInt("id"), rs.getString("nombre"));
		}
		rs.close();
		st.close();

	}

	private void obtenerFarmacos() throws SQLException {
		System.out
				.println("Lista completa de fármacos que hay en la base de datos:" + '\n');
		String format = "|%1$-5s|%2$-20s|%3$-20s|%4$-30s|\n";
		System.out.format(format, "ID", "Nombre", "Principio Activo",
				"Excipientes");
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT medicamento.id, medicamento.nombre, principio_activo.nombre as nombre_principio_activo "
						+ "FROM medicamento "
						+ "INNER JOIN principio_activo ON medicamento.id_principio_activo = principio_activo.id");
		while (rs.next()) {
			int idFarmaco = rs.getInt("id");
			System.out.format(format, "--", "------", "----------------",
					"-----------");
			System.out.format(format, idFarmaco, rs.getString("nombre"),
					rs.getString("nombre_principio_activo"), "");
			System.out.format(format, "--", "------", "----------------", "");
			// Excipìentes
			Statement st_2 = conn.createStatement();
			ResultSet rs_2 = st_2
					.executeQuery("SELECT distinct excipiente.nombre"
							+ " FROM clinica.excipiente where excipiente.id "
							+ "IN (SELECT id_excipiente FROM clinica.tiene_excipiente  where id_medicamento = "
							+ idFarmaco + ")");
			while (rs_2.next()) {
				System.out.format(format, "", "", "", rs_2.getString("nombre"));
			}
			rs_2.close();
			st_2.close();
		}
		rs.close();
		st.close();
	}

	private void obtenerEnfermedades() throws SQLException {
		System.out
				.println("Lista completa de enfermedades que hay en la base de datos:" + '\n');
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM enfermedad");
		String format = "|%1$-5s|%2$-15s|%3$-25s|\n";
		System.out.format(format, "ID", "Nombre", "Sintoma");
		while (rs.next()) {
			int idEnfermedad = rs.getInt("id");
			System.out.format(format, "--", "------", "-------");
			System.out.format(format, idEnfermedad, rs.getString("nombre"), "");
			System.out.format(format, "--", "------", "-------");

			// Sintomas de la enfermedad
			Statement st_2 = conn.createStatement();
			ResultSet rs_2 = st_2
					.executeQuery("SELECT distinct sintomas.nombre "
							+ "FROM clinica.sintomas where sintomas.id "
							+ "IN (SELECT id_sintoma FROM clinica.caracteriza  where id_enfermedad = "
							+ idEnfermedad + ")");
			while (rs_2.next()) {
				System.out.format(format, "", "", rs_2.getString("nombre"));
			}
			rs_2.close();
			st_2.close();

		}
		rs.close();
		st.close();
	}

	private void obtenerPacientes() throws SQLException {
		System.out.println("Lista de pacientes disponibles:" + '\n');
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT paciente.id, paciente.nombre, doctor.nombre as nombre_doctor, doctor.id as id_doctor"
						+ " FROM paciente "
						+ "	 INNER JOIN doctor"
						+ " ON paciente.id_doctor = doctor.id");
		String format = "|%1$-5s|%2$-10s|%3$-10s|%4$-15s|\n";
		System.out.format(format, "ID", "Nombre", "ID Doctor", "Nombre Doctor");
		System.out.format(format, "--", "------", "--------", "--------------");
		while (rs.next()) {
			System.out.format(format, rs.getInt("id"), rs.getString("nombre"),
					rs.getInt("id_doctor"), rs.getString("nombre_doctor"));
		}
		rs.close();
		st.close();

	}

	private void obtenerDoctores() throws SQLException {
		System.out
				.println("Lista completa de doctores que hay en la base de datos:" + '\n');
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM doctor");
		String format = "|%1$-10s|%2$-10s|\n";
		System.out.format(format, "ID", "Nombre");
		System.out.format(format, "--", "------");
		while (rs.next()) {
			System.out.format(format, rs.getInt("id"), rs.getString("nombre"));

		}
		rs.close();
		st.close();

	}

	private int readInt() throws Exception {
		try {
			System.out.print("> ");
			return Integer.parseInt(new BufferedReader(new InputStreamReader(
					System.in)).readLine());
		} catch (Exception e) {
			throw new Exception("Not number");
		}
	}

	private String readString() throws Exception {
		try {
			System.out.print("> ");
			return new BufferedReader(new InputStreamReader(System.in))
					.readLine();
		} catch (Exception e) {
			throw new Exception("Error reading line");
		}
	}

	private void printState(SQLException esql) {
		System.err.println("Mensaje: " + esql.getMessage());
		System.err.println("Código: " + esql.getErrorCode());
		System.err.println("Estado SQL: " + esql.getSQLState());
	}

	public static void main(String args[]) throws Exception {
		new Clinica().showMenu();
	}
}
