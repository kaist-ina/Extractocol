// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package in.shick.diode.things;


// Referenced classes of package in.shick.diode.things:
//            ListingData

public class Listing
{

    public Listing()
    {
    }

    public Listing(String s)
    {
        kind = null;
        data = null;
    }

    public ListingData getData()
    {
        return data;
    }

    public String getKind()
    {
        return kind;
    }

    public void setData(ListingData listingdata)
    {
        data = listingdata;
    }

    public void setKind(String s)
    {
        kind = s;
    }

    private ListingData data;
    private String kind;
}
