const toc2 = document.querySelector('#toc.toc2');
const selected = toc2.querySelector('li > p > a.current');
toc2.scrollTop = selected.offsetTop - toc2.offsetTop;