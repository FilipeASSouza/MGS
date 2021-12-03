SELECT
'F' ORIGEM
, TGFFIN.NUFIN
, RATEIO.NATUREZA_OPR CODNAT
, TGFFIN.CODCENCUS
, TGFFIN.CODPROJ
, ROUND( (rateio.vlrdesdob * 100) / SUM(rateio.vlrdesdob) OVER( PARTITION BY rateio.cod_autorizacao_viagem ),2) PERCRATEIO
, AD_TGFNATCCCR.CODCTACTB
, 0 NUMCONTRATO
, 'N' DIGITADO
, RATEIO.UNIDADE CODSITE
, TGFFIN.CODPARC
, 0 CODUSU
, TO_DATE( TGFFIN.DTNEG, 'DD/MM/YYYY' ) DTALTER
FROM
  ( SELECT
    autorizacao_viagem.cod_autorizacao_viagem,
    tipo_despesa_viagem.natureza_opr,
    rhpess_contrato.cpf ad_cnpjcpfparc,
    TO_CHAR(autorizacao_viagem.dat_aprovacao2,'DD/MM/YYYY') dtneg,
    TO_CHAR(autorizacao_viagem.dat_aprovacao2,'DD/MM/YYYY') dtvenc,
    DECODE(autorizacao_viagem.cod_banco + 0,1,1,237,237,104,104,1) codbco,
    NULL codctabcoint,
    20 codtiptit,
    SUBSTR(autorizacao_viagem.codigo,10,6) ad_matricula,
    SUBSTR(autorizacao_viagem.cod_unidade1,4,3)
    || SUBSTR(autorizacao_viagem.cod_unidade2,4,3)
    || SUBSTR(autorizacao_viagem.cod_unidade3,4,3) unidade,
    autorizacao_viagem.cod_banco + 0 ad_codbcoparc,
    autorizacao_viagem.cod_agencia ad_agenciaparceiro,
    autorizacao_viagem.num_conta ad_contaparceiro,
    SUM(acerto_viagem.vlr_acerto_viagem) vlrdesdob
  FROM autorizacao_viagem@DLINK_MGS,
    acerto_viagem@DLINK_MGS,
    tipo_despesa_viagem@DLINK_MGS,
    rhpess_contrato@DLINK_MGS
  WHERE autorizacao_viagem.cod_autorizacao_viagem        = acerto_viagem.cod_autorizacao_viagem
  AND acerto_viagem.cod_tipo_despesa_viagem              = tipo_despesa_viagem.cod_tipo_despesa_viagem
  AND autorizacao_viagem.dat_confirmacao_autorizacao_vi >= TO_DATE('01/01/2018','DD/MM/YYYY')
  --AND autorizacao_viagem.cod_autorizacao_viagem          =:COD_AUTORIZACAO_VIAGEM
  AND autorizacao_viagem.codigo                          = rhpess_contrato.codigo
  GROUP BY autorizacao_viagem.cod_autorizacao_viagem,
    tipo_despesa_viagem.natureza_opr,
    rhpess_contrato.cpf,
    TO_CHAR(autorizacao_viagem.dat_aprovacao2,'DD/MM/YYYY'),
    TO_CHAR(autorizacao_viagem.dat_aprovacao2,'DD/MM/YYYY'),
    DECODE(autorizacao_viagem.cod_banco + 0,1,1,237,237,104,104,1),
    NULL,
    20,
    SUBSTR(autorizacao_viagem.codigo,10,6),
    SUBSTR(autorizacao_viagem.cod_unidade1,4,3)
    || SUBSTR(autorizacao_viagem.cod_unidade2,4,3)
    || SUBSTR(autorizacao_viagem.cod_unidade3,4,3),
    autorizacao_viagem.cod_banco + 0,
    autorizacao_viagem.cod_agencia,
    autorizacao_viagem.num_conta
  ) rateio
  INNER JOIN TGFFIN ON TGFFIN.NUMNOTA = RATEIO.COD_AUTORIZACAO_VIAGEM AND RECDESP = -1 AND CODTIPOPER = 1030 AND TGFFIN.NUCOMPENS IS NOT NULL
  INNER JOIN AD_TGFNATCCCR ON AD_TGFNATCCCR.CODNAT = RATEIO.NATUREZA_OPR AND AD_TGFNATCCCR.NUCLASSIFICACAO = 2
  LEFT JOIN TGFRAT ON TGFRAT.NUFIN = TGFFIN.NUFIN AND TGFRAT.ORIGEM = 'F'
  WHERE
  EXTRACT( MONTH FROM TGFFIN.DTNEG ) >= EXTRACT( MONTH FROM SYSDATE -1 )
  AND EXTRACT( YEAR FROM TGFFIN.DTNEG ) >= EXTRACT( YEAR FROM SYSDATE )
  AND TGFFIN.RATEADO = 'N'
  ORDER BY TGFFIN.NUFIN, TGFFIN.DTNEG ASC