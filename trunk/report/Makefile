PRY=electrolites
SRC=$(PRY).tex
PDF=$(PRY).pdf

.PHONY: show

plain:
	pdflatex plain-style.tex
	evince plain-style.pdf

all:
	pdflatex $(SRC)
	pdflatex $(SRC)
	pdflatex $(SRC)
	
show: all
	evince $(PDF)
	
win: all
	# taskkill /IM "Foxit Reader.exe"
	# "C:\Program Files\Foxit Software\Foxit Reader\Foxit Reader.exe" $(PDF) &
	"C:\Program Files (x86)\Adobe\Acrobat 9.0\Acrobat\Acrobat.exe" $(PDF) &
	
clean:
	rm -f *.log *.aux *.toc *.lof *.lot *.out
