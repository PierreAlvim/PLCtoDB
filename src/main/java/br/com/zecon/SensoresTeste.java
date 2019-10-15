package br.com.zecon;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "sensores_teste")
public class SensoresTeste implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", insertable = false, nullable = false)
  private Integer id;

  @NotNull
  @Column(name = "time", nullable = false)
  private Timestamp time;

  @NotNull
  @Column(name = "velocidade_eixo", nullable = false)
  private Float velocidadeEixo;

  @NotNull
  @Column(name = "inclinacao_dist_1", nullable = false)
  private Float inclinacaoDist1;

  @NotNull
  @Column(name = "inclinacao_dist_2", nullable = false)
  private Float inclinacaoDist2;

  @NotNull
  @Column(name = "valv_subida_1", nullable = false)
  private Boolean valvSubida1;

  @NotNull
  @Column(name = "valv_subida_2", nullable = false)
  private Boolean valvSubida2;

  @NotNull
  @Column(name = "valv_parada_1", nullable = false)
  private Boolean valvParada1;

  @NotNull
  @Column(name = "valv_parada_2", nullable = false)
  private Boolean valvParada2;

  
}