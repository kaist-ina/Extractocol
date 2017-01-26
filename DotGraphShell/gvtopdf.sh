ROOT="$1*.gv"
mkdir "$1/pdfs"
for f in $ROOT
do
	echo "$f.pdf"
	dot -Tpdf $f > "$1/pdfs/${f##*/}.pdf"
done
