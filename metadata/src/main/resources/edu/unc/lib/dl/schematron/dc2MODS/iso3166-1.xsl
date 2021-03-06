<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:variable name="iso3166-1">
        <!-- valid ISO 3166-1 2-letter country codes -->
        <xsl:text>AF</xsl:text>
        <xsl:text>AL</xsl:text>
        <xsl:text>DZ</xsl:text>
        <xsl:text>AS</xsl:text>
        <xsl:text>AD</xsl:text>
        <xsl:text>AO</xsl:text>
        <xsl:text>AI</xsl:text>
        <xsl:text>AQ</xsl:text>
        <xsl:text>AG</xsl:text>
        <xsl:text>AR</xsl:text>
        <xsl:text>AM</xsl:text>
        <xsl:text>AW</xsl:text>
        <xsl:text>AU</xsl:text>
        <xsl:text>AT</xsl:text>
        <xsl:text>AZ</xsl:text>
        <xsl:text>BS</xsl:text>
        <xsl:text>BH</xsl:text>
        <xsl:text>BD</xsl:text>
        <xsl:text>BB</xsl:text>
        <xsl:text>BY</xsl:text>
        <xsl:text>BE</xsl:text>
        <xsl:text>BZ</xsl:text>
        <xsl:text>BJ</xsl:text>
        <xsl:text>BM</xsl:text>
        <xsl:text>BT</xsl:text>
        <xsl:text>BO</xsl:text>
        <xsl:text>BA</xsl:text>
        <xsl:text>BW</xsl:text>
        <xsl:text>BV</xsl:text>
        <xsl:text>BR</xsl:text>
        <xsl:text>IO</xsl:text>
        <xsl:text>VG</xsl:text>
        <xsl:text>BN</xsl:text>
        <xsl:text>BG</xsl:text>
        <xsl:text>BF</xsl:text>
        <xsl:text>BI</xsl:text>
        <xsl:text>KH</xsl:text>
        <xsl:text>CM</xsl:text>
        <xsl:text>CA</xsl:text>
        <xsl:text>CV</xsl:text>
        <xsl:text>KY</xsl:text>
        <xsl:text>CF</xsl:text>
        <xsl:text>TD</xsl:text>
        <xsl:text>CL</xsl:text>
        <xsl:text>CN</xsl:text>
        <xsl:text>CX</xsl:text>
        <xsl:text>CC</xsl:text>
        <xsl:text>CO</xsl:text>
        <xsl:text>KM</xsl:text>
        <xsl:text>CD</xsl:text>
        <xsl:text>CG</xsl:text>
        <xsl:text>CK</xsl:text>
        <xsl:text>CR</xsl:text>
        <xsl:text>CI</xsl:text>
        <xsl:text>CU</xsl:text>
        <xsl:text>CY</xsl:text>
        <xsl:text>CZ</xsl:text>
        <xsl:text>DK</xsl:text>
        <xsl:text>DJ</xsl:text>
        <xsl:text>DM</xsl:text>
        <xsl:text>DO</xsl:text>
        <xsl:text>EC</xsl:text>
        <xsl:text>EG</xsl:text>
        <xsl:text>SV</xsl:text>
        <xsl:text>GQ</xsl:text>
        <xsl:text>ER</xsl:text>
        <xsl:text>EE</xsl:text>
        <xsl:text>ET</xsl:text>
        <xsl:text>FO</xsl:text>
        <xsl:text>FK</xsl:text>
        <xsl:text>FJ</xsl:text>
        <xsl:text>FI</xsl:text>
        <xsl:text>FR</xsl:text>
        <xsl:text>GF</xsl:text>
        <xsl:text>PF</xsl:text>
        <xsl:text>TF</xsl:text>
        <xsl:text>GA</xsl:text>
        <xsl:text>GM</xsl:text>
        <xsl:text>GE</xsl:text>
        <xsl:text>DE</xsl:text>
        <xsl:text>GH</xsl:text>
        <xsl:text>GI</xsl:text>
        <xsl:text>GR</xsl:text>
        <xsl:text>GL</xsl:text>
        <xsl:text>GD</xsl:text>
        <xsl:text>GP</xsl:text>
        <xsl:text>GU</xsl:text>
        <xsl:text>GT</xsl:text>
        <xsl:text>GN</xsl:text>
        <xsl:text>GW</xsl:text>
        <xsl:text>GY</xsl:text>
        <xsl:text>HT</xsl:text>
        <xsl:text>HM</xsl:text>
        <xsl:text>VA</xsl:text>
        <xsl:text>HN</xsl:text>
        <xsl:text>HK</xsl:text>
        <xsl:text>HR</xsl:text>
        <xsl:text>HU</xsl:text>
        <xsl:text>IS</xsl:text>
        <xsl:text>IN</xsl:text>
        <xsl:text>ID</xsl:text>
        <xsl:text>IR</xsl:text>
        <xsl:text>IQ</xsl:text>
        <xsl:text>IE</xsl:text>
        <xsl:text>IL</xsl:text>
        <xsl:text>IT</xsl:text>
        <xsl:text>JM</xsl:text>
        <xsl:text>JP</xsl:text>
        <xsl:text>JO</xsl:text>
        <xsl:text>KZ</xsl:text>
        <xsl:text>KE</xsl:text>
        <xsl:text>KI</xsl:text>
        <xsl:text>KP</xsl:text>
        <xsl:text>KR</xsl:text>
        <xsl:text>KW</xsl:text>
        <xsl:text>KG</xsl:text>
        <xsl:text>LA</xsl:text>
        <xsl:text>LV</xsl:text>
        <xsl:text>LB</xsl:text>
        <xsl:text>LS</xsl:text>
        <xsl:text>LR</xsl:text>
        <xsl:text>LY</xsl:text>
        <xsl:text>LI</xsl:text>
        <xsl:text>LT</xsl:text>
        <xsl:text>LU</xsl:text>
        <xsl:text>MO</xsl:text>
        <xsl:text>MK</xsl:text>
        <xsl:text>MG</xsl:text>
        <xsl:text>MW</xsl:text>
        <xsl:text>MY</xsl:text>
        <xsl:text>MV</xsl:text>
        <xsl:text>ML</xsl:text>
        <xsl:text>MT</xsl:text>
        <xsl:text>MH</xsl:text>
        <xsl:text>MQ</xsl:text>
        <xsl:text>MR</xsl:text>
        <xsl:text>MU</xsl:text>
        <xsl:text>YT</xsl:text>
        <xsl:text>MX</xsl:text>
        <xsl:text>FM</xsl:text>
        <xsl:text>MD</xsl:text>
        <xsl:text>MC</xsl:text>
        <xsl:text>MN</xsl:text>
        <xsl:text>MS</xsl:text>
        <xsl:text>MA</xsl:text>
        <xsl:text>MZ</xsl:text>
        <xsl:text>MM</xsl:text>
        <xsl:text>NA</xsl:text>
        <xsl:text>NR</xsl:text>
        <xsl:text>NP</xsl:text>
        <xsl:text>AN</xsl:text>
        <xsl:text>NL</xsl:text>
        <xsl:text>NC</xsl:text>
        <xsl:text>NZ</xsl:text>
        <xsl:text>NI</xsl:text>
        <xsl:text>NE</xsl:text>
        <xsl:text>NG</xsl:text>
        <xsl:text>NU</xsl:text>
        <xsl:text>NF</xsl:text>
        <xsl:text>MP</xsl:text>
        <xsl:text>NO</xsl:text>
        <xsl:text>OM</xsl:text>
        <xsl:text>PK</xsl:text>
        <xsl:text>PW</xsl:text>
        <xsl:text>PS</xsl:text>
        <xsl:text>PA</xsl:text>
        <xsl:text>PG</xsl:text>
        <xsl:text>PY</xsl:text>
        <xsl:text>PE</xsl:text>
        <xsl:text>PH</xsl:text>
        <xsl:text>PN</xsl:text>
        <xsl:text>PL</xsl:text>
        <xsl:text>PT</xsl:text>
        <xsl:text>PR</xsl:text>
        <xsl:text>QA</xsl:text>
        <xsl:text>RE</xsl:text>
        <xsl:text>RO</xsl:text>
        <xsl:text>RU</xsl:text>
        <xsl:text>RW</xsl:text>
        <xsl:text>SH</xsl:text>
        <xsl:text>KN</xsl:text>
        <xsl:text>LC</xsl:text>
        <xsl:text>PM</xsl:text>
        <xsl:text>VC</xsl:text>
        <xsl:text>WS</xsl:text>
        <xsl:text>SM</xsl:text>
        <xsl:text>ST</xsl:text>
        <xsl:text>SA</xsl:text>
        <xsl:text>SN</xsl:text>
        <xsl:text>CS</xsl:text>
        <xsl:text>SC</xsl:text>
        <xsl:text>SL</xsl:text>
        <xsl:text>SG</xsl:text>
        <xsl:text>SK</xsl:text>
        <xsl:text>SI</xsl:text>
        <xsl:text>SB</xsl:text>
        <xsl:text>SO</xsl:text>
        <xsl:text>ZA</xsl:text>
        <xsl:text>GS</xsl:text>
        <xsl:text>ES</xsl:text>
        <xsl:text>LK</xsl:text>
        <xsl:text>SD</xsl:text>
        <xsl:text>SR</xsl:text>
        <xsl:text>SJ</xsl:text>
        <xsl:text>SZ</xsl:text>
        <xsl:text>SE</xsl:text>
        <xsl:text>CH</xsl:text>
        <xsl:text>SY</xsl:text>
        <xsl:text>TW</xsl:text>
        <xsl:text>TJ</xsl:text>
        <xsl:text>TZ</xsl:text>
        <xsl:text>TH</xsl:text>
        <xsl:text>TL</xsl:text>
        <xsl:text>TG</xsl:text>
        <xsl:text>TK</xsl:text>
        <xsl:text>TO</xsl:text>
        <xsl:text>TT</xsl:text>
        <xsl:text>TN</xsl:text>
        <xsl:text>TR</xsl:text>
        <xsl:text>TM</xsl:text>
        <xsl:text>TC</xsl:text>
        <xsl:text>TV</xsl:text>
        <xsl:text>VI</xsl:text>
        <xsl:text>UG</xsl:text>
        <xsl:text>UA</xsl:text>
        <xsl:text>AE</xsl:text>
        <xsl:text>GB</xsl:text>
        <xsl:text>UM</xsl:text>
        <xsl:text>US</xsl:text>
        <xsl:text>UY</xsl:text>
        <xsl:text>UZ</xsl:text>
        <xsl:text>VU</xsl:text>
        <xsl:text>VE</xsl:text>
        <xsl:text>VN</xsl:text>
        <xsl:text>WF</xsl:text>
        <xsl:text>EH</xsl:text>
        <xsl:text>YE</xsl:text>
        <xsl:text>ZM</xsl:text>
        <xsl:text>ZW</xsl:text>
    </xsl:variable>
</xsl:stylesheet>