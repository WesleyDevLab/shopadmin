package com.ibiscus.shopnchek.domain.admin;


public class Visita {

  private String shopperDni;

  private String programa;

  private String local;

  private String nombre;

  private Integer asignacion;

  private Integer mes;

  private Integer anio;

  private String fecha;

  private String descripcion;

  private String fechaCobro;

  private double importe;

  private int tipoItem;

  private int tipoPago;

  public Visita(final String unShopperDni,
      final String unPrograma, final String unLocal, final String unNombre,
      final Integer unaAsignacion, final int unMes, final int unAnio,
      final String unaFecha,
      final String unaDescripcion, final String unaFechaCobro,
      final double unImporte, final Integer unTipoItem, final int unTipoPago) {
    shopperDni = unShopperDni;
    programa = unPrograma;
    local = unLocal;
    nombre = unNombre;
    asignacion = unaAsignacion;
    mes = unMes;
    anio = unAnio;
    fecha = unaFecha;
    descripcion = unaDescripcion;
    fechaCobro = unaFechaCobro;
    importe = unImporte;
    tipoPago = unTipoPago;
    tipoItem = unTipoItem;
  }

  /**
   * @return the shopperDni
   */
  public String getShopperDni() {
    return shopperDni;
  }

  /**
   * @return the programa
   */
  public String getPrograma() {
    return programa;
  }

  /**
   * @return the local
   */
  public String getLocal() {
    return local;
  }

  /**
   * @return the nombre
   */
  public String getNombre() {
    return nombre;
  }

  public Integer getAsignacion() {
    return asignacion;
  }

  /**
   * @return the mes
   */
  public Integer getMes() {
    return mes;
  }

  /**
   * @return the anio
   */
  public Integer getAnio() {
    return anio;
  }

  /**
   * @return the fecha
   */
  public String getFecha() {
    return fecha;
  }

  /**
   * @return the descripcion
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * @return the fechaCobro
   */
  public String getFechaCobro() {
    return fechaCobro;
  }

  /**
   * @return the importe
   */
  public double getImporte() {
    return importe;
  }

  /**
   * @return the tipoItem
   */
  public int getTipoItem() {
    return tipoItem;
  }

  /**
   * @return the tipoPago
   */
  public int getTipoPago() {
    return tipoPago;
  }
}