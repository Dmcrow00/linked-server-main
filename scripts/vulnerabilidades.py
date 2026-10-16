"""Dependencias con vulnerabilidades conocidas (F16 · C11), consultadas en la base pública de OSV (osv.dev).

Lee el SBOM CycloneDX que genera Maven (target/sbom.json) o el package-lock.json del cliente, pregunta a OSV por cada
paquete y versión, y falla si hay alguna vulnerabilidad de severidad alta o crítica (CVSS 7 o más) que no esté aceptada
en el archivo de excepciones (una por línea: "ID  motivo y fecha de revisión").

Uso:
    python scripts/vulnerabilidades.py target/sbom.json [--aceptadas .vulnerabilidades-aceptadas]
    python scripts/vulnerabilidades.py package-lock.json

Sólo usa la biblioteca estándar: corre igual en la integración continua y en una PC.
"""
import json
import sys
import urllib.request

OSV = "https://api.osv.dev/v1"
UMBRAL = 7.0


def paquetes(ruta):
    datos = json.load(open(ruta, encoding="utf-8"))
    if "components" in datos:  # CycloneDX
        for c in datos["components"]:
            if c.get("group") and c.get("version"):
                yield "Maven", f'{c["group"]}:{c["name"]}', c["version"]
    elif "packages" in datos:  # package-lock.json v2/v3
        for camino, p in datos["packages"].items():
            if camino and p.get("version") and not p.get("dev"):
                yield "npm", camino.split("node_modules/")[-1], p["version"]
    else:
        sys.exit(f"No reconozco el formato de {ruta}")


def pedir(url, cuerpo=None):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    req = urllib.request.Request(url, data=datos, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def puntaje(vuln):
    """El puntaje CVSS más alto que informe la vulnerabilidad; si sólo hay una severidad en texto, se traduce."""
    mejor = 0.0
    for s in vuln.get("severity", []):
        vector = s.get("score", "")
        if vector.startswith("CVSS:3"):
            try:
                mejor = max(mejor, cvss3(vector))
            except (KeyError, ValueError):
                pass
    texto = (vuln.get("database_specific") or {}).get("severity", "")
    mejor = max(mejor, {"CRITICAL": 9.0, "HIGH": 7.5, "MODERATE": 5.0, "MEDIUM": 5.0, "LOW": 2.0}.get(texto.upper(), 0.0))
    return mejor


def cvss3(vector):
    """Puntaje base CVSS 3.x a partir del vector (fórmula de FIRST)."""
    import math
    m = dict(p.split(":") for p in vector.split("/")[1:] if ":" in p)
    if "AV" not in m:
        return 0.0
    av = {"N": 0.85, "A": 0.62, "L": 0.55, "P": 0.2}[m["AV"]]
    ac = {"L": 0.77, "H": 0.44}[m.get("AC", "L")]
    cambio = m.get("S", "U") == "C"
    pr = {"N": 0.85, "L": 0.68 if cambio else 0.62, "H": 0.5 if cambio else 0.27}[m.get("PR", "N")]
    ui = {"N": 0.85, "R": 0.62}[m.get("UI", "N")]
    cia = [{"H": 0.56, "L": 0.22, "N": 0.0}[m.get(k, "N")] for k in ("C", "I", "A")]
    iss = 1 - (1 - cia[0]) * (1 - cia[1]) * (1 - cia[2])
    impacto = 7.52 * (iss - 0.029) - 3.25 * (iss - 0.02) ** 15 if cambio else 6.42 * iss
    if impacto <= 0:
        return 0.0
    explotabilidad = 8.22 * av * ac * pr * ui
    base = min(1.08 * (impacto + explotabilidad), 10) if cambio else min(impacto + explotabilidad, 10)
    return math.ceil(base * 10 - 1e-9) / 10


def main():
    args = sys.argv[1:]
    if not args:
        sys.exit(__doc__)
    aceptadas = set()
    if "--aceptadas" in args:
        i = args.index("--aceptadas")
        try:
            for linea in open(args[i + 1], encoding="utf-8"):
                if linea.strip() and not linea.startswith("#"):
                    aceptadas.add(linea.split()[0])
        except FileNotFoundError:
            pass
        del args[i:i + 2]

    lista = sorted(set(paquetes(args[0])))
    encontradas = {}
    for i in range(0, len(lista), 500):
        tanda = lista[i:i + 500]
        r = pedir(f"{OSV}/querybatch", {"queries": [
            {"package": {"ecosystem": e, "name": n}, "version": v} for e, n, v in tanda]})
        for (e, n, v), res in zip(tanda, r["results"]):
            for vuln in res.get("vulns", []):
                encontradas.setdefault(vuln["id"], []).append(f"{n}@{v}")

    graves = []
    for vid, donde in sorted(encontradas.items()):
        vuln = pedir(f"{OSV}/vulns/{vid}")
        p = puntaje(vuln)
        alias = ",".join(a for a in vuln.get("aliases", []) if a.startswith("CVE"))[:40]
        estado = "aceptada" if vid in aceptadas or any(a in aceptadas for a in vuln.get("aliases", [])) else ""
        print(f"{p:4.1f}  {vid:22} {alias:18} {', '.join(sorted(set(donde)))[:90]}  {estado}")
        if p >= UMBRAL and not estado:
            graves.append(vid)

    print(f"\n{len(lista)} paquetes revisados, {len(encontradas)} vulnerabilidades conocidas, "
          f"{len(graves)} altas o críticas sin aceptar.")
    sys.exit(1 if graves else 0)


if __name__ == "__main__":
    main()
