<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                exclude-result-prefixes="soap">
    <xsl:output method="xml" indent="yes"/>

    <!-- Plantilla para copiar el documento de entrada -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Plantilla para cambiar el valor de 'Fecha' -->
    <xsl:template match="Fecha">
        <xsl:copy>
            <xsl:text>Nueva Fecha</xsl:text>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
