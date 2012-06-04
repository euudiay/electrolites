PRY=electrolites
SRC=$(PRY).tex
PDF=$(PRY).pdf

.PHONY: show

all:
	pdflatex $(SRC)
	pdflatex $(SRC)
	pdflatex $(SRC)
	
show: all
	evince $(PDF)
	
win: all
	# taskkill /IM "Foxit Reader.exe"
	"C:\Program Files\Foxit Software\Foxit Reader\Foxit Reader.exe" $(PDF) &
	
clean:
	rm -f *.log *.aux *.toc *.lof *.lot *.out
