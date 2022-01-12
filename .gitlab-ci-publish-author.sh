#!/bin/bash

# the script translates GitLab authors to GitHub in order to hide real E-Mails
# by mock E-Mail from here: https://github.com/settings/emails
# or not existing in domain bgerp.org, added to a user's GitHub account

AUTHOR=`git show -s --format='%an <%ae>'`

# https://git.pzdc.de/admin/users
if [[ $AUTHOR =~ "shamil@ufamail.ru" ]]; then
    echo "Shamil Vakhitov <shamil@bgerp.org>"
elif [[ $AUTHOR =~ "ildar-men@mail.ru" ]]; then
    echo "Ildar Fattakhov <ildar@bgerp.org>"
elif [[ $AUTHOR =~ "borisff2003@list.ru" ]]; then
    echo "Boris Fedorako <boris@bgerp.org>"
elif [[ $AUTHOR =~ "zavndw@bghelp.ru" ]]; then
    echo "Andrey Zuzenkov <andrey@bgerp.org>"
elif [[ $AUTHOR =~ "zhenyab@yahoo.com" ]]; then
    echo "Eugene Bogomolny <1694830+zhenyab@users.noreply.github.com>"
else
    echo $AUTHOR
fi

# translated authors

# Shamil Vakhitov <shamil@ufamail.ru>
# Ildar Fattakhov <ildar-men@mail.ru>
# Boris Fedorako <borisff2003@list.ru>
# Andrey Zuzenkov <zavndw@bghelp.ru>
# Eugene Bogomolny <zhenyab@yahoo.com>

# without translation

# Pavel Mozhar <passkeykz@gmail.com>
# Amir Asfandyarov <28504819+omni1504@users.noreply.github.com>
# Nelli Vakhitova <75040785+nellivahitova@users.noreply.github.com>
# Dina Vakhitova <75733993+dina235@users.noreply.github.com>
# Andrey Kolybelnikov <outman@mail.ru>
# Iakov Volkov <baldur13@ya.ru>
# Michael Kozlov <kozlov.ufanet.ru@gmail.com>
# Artur Gareev <reflexive007@yandex.ru>
# Sergey Sizov <wolf-sf@mail.ru>
# Kirill Rozhenkov <4720818+grede@users.noreply.github.com>
# Denis Merkulov <merkleen@gmail.com>
# Artur Kamalov <avk@ufanet.ru>
# Vyacheslav Osokin <vnosokin@yandex.ru>
# Alexander Yaryzhenko <92966572+arj57@users.noreply.github.com>
