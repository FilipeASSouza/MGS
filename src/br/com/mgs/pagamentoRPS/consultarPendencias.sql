SELECT
*
FROM AD_TSIFILINT
WHERE
INSTANCIA = 'Financeiro'
AND OPERACAO = 'BAIXA'
AND STATUSPROC <> 'Concluido'
AND CODTIPOPER = 1010
AND DHPROC IS NOT NULL
AND ( DHLANC > ( SYSDATE - 2 ) )